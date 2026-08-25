package com.mapmory.shared.presentation.map.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.LocationType
import com.mapmory.shared.domain.model.TripRecordQuery
import com.mapmory.shared.domain.region.RegionCatalog
import com.mapmory.shared.domain.usecase.GetTripRecordsUseCase
import com.mapmory.shared.presentation.map.data.GeneratedKoreaDistrictMapData
import com.mapmory.shared.presentation.map.domain.MapScope
import com.mapmory.shared.presentation.map.state.KoreaMapUiState

data class MapUiState(
    val scope: MapScope = MapScope.KOREA,
    val koreaMap: KoreaMapUiState = KoreaMapUiState.ProvinceOverview,
    val visitedLocationIds: Set<Long> = emptySet(),
    val errorMessage: String? = null,
)

class MapViewModel(
    private val getTripRecords: GetTripRecordsUseCase,
    private val regionCatalog: RegionCatalog,
) : ViewModel() {
    private val locationsById = regionCatalog.locations.associateBy(Location::id)

    var uiState by mutableStateOf(MapUiState())
        private set

    suspend fun refresh() {
        val visitedIds = mutableSetOf<Long>()
        var page = 0
        do {
            val result = getTripRecords(TripRecordQuery(page = page, size = PageSize))
            val recordPage = result.getOrElse { error ->
                uiState = uiState.copy(
                    errorMessage = error.message ?: "지도 기록을 불러오지 못했습니다.",
                )
                return
            }
            visitedIds += recordPage.records.map { it.locationId }
            page += 1
        } while (page < recordPage.totalPages)

        uiState = uiState.copy(
            visitedLocationIds = visitedIds,
            errorMessage = null,
        )
    }

    fun changeScope(scope: MapScope) {
        uiState = uiState.copy(
            scope = scope,
            koreaMap = KoreaMapUiState.ProvinceOverview,
        )
    }

    suspend fun openProvince(provinceCode: String) {
        uiState = uiState.copy(koreaMap = KoreaMapUiState.DistrictLoading(provinceCode))
        uiState = uiState.copy(
            koreaMap = runCatching {
                GeneratedKoreaDistrictMapData.forProvince(provinceCode)
            }.fold(
                onSuccess = { regions ->
                    KoreaMapUiState.DistrictDetail(provinceCode, regions)
                },
                onFailure = { error ->
                    KoreaMapUiState.Error(
                        provinceCode = provinceCode,
                        message = error.message ?: "시·군·구 지도를 불러오지 못했습니다.",
                    )
                },
            ),
        )
    }

    fun closeProvince(): Boolean {
        if (uiState.koreaMap == KoreaMapUiState.ProvinceOverview) return false
        uiState = uiState.copy(koreaMap = KoreaMapUiState.ProvinceOverview)
        return true
    }

    fun hasRecords(location: Location): Boolean = uiState.visitedLocationIds.any { locationId ->
        locationContains(location, locationsById[locationId])
    }

    val visitedCountryCodes: Set<String>
        get() = visitedLocations().map { location ->
            if (location.countryId == KoreaCountryId) "KR" else location.regionCode
        }.toSet()

    val visitedProvinceCodes: Set<String>
        get() = visitedLocations().mapNotNull { location ->
            when {
                location.countryId != KoreaCountryId -> null
                location.type == LocationType.PROVINCE -> location.regionCode
                else -> locationsById[location.parentId]?.regionCode
            }
        }.toSet()

    fun visitedDistrictCodes(provinceCode: String): Set<String> = visitedLocations()
        .filter { location ->
            location.type == LocationType.DISTRICT &&
                locationsById[location.parentId]?.regionCode == provinceCode
        }
        .map(Location::regionCode)
        .toSet()

    fun visitedDistrictCount(provinceCode: String): Int = visitedDistrictCodes(provinceCode).size

    private fun visitedLocations(): List<Location> =
        uiState.visitedLocationIds.mapNotNull(locationsById::get)

    private fun locationContains(selected: Location, recordLocation: Location?): Boolean {
        recordLocation ?: return false
        return when {
            selected.regionCode == "KR" -> recordLocation.countryId == KoreaCountryId
            selected.countryId == KoreaCountryId && selected.type == LocationType.PROVINCE ->
                recordLocation.id == selected.id || recordLocation.parentId == selected.id
            else -> recordLocation.id == selected.id
        }
    }
}

private const val KoreaCountryId = 1L
private const val PageSize = 100
