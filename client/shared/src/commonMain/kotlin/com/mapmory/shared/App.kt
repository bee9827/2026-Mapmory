package com.mapmory.shared

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mapmory.shared.domain.model.KoreanCountryNames
import com.mapmory.shared.domain.model.KoreanSelectableDistrictCodes
import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.LocationType
import com.mapmory.shared.data.remote.createHttpClient
import com.mapmory.shared.presentation.map.data.GeneratedKoreaMapData
import com.mapmory.shared.presentation.map.data.KoreaMapRemoteDataSource
import com.mapmory.shared.presentation.map.data.GeneratedWorldMapData
import com.mapmory.shared.presentation.map.domain.MapScope
import com.mapmory.shared.presentation.map.domain.ProvincePolygon
import com.mapmory.shared.presentation.map.domain.geoJsonProvinceCodeForServer
import com.mapmory.shared.presentation.map.domain.serverProvinceCodeForGeoJson
import com.mapmory.shared.presentation.map.state.KoreaMapUiState
import com.mapmory.shared.presentation.map.ui.MapArtwork
import com.mapmory.shared.presentation.map.ui.KoreaMapStatusMessage
import com.mapmory.shared.presentation.triprecord.screen.TripMapScreen
import com.mapmory.shared.presentation.triprecord.screen.TripProfileScreen
import com.mapmory.shared.presentation.triprecord.screen.TripRecordDetailScreen
import com.mapmory.shared.presentation.triprecord.screen.TripRecordEditorScreen
import com.mapmory.shared.presentation.triprecord.screen.TripRecordListScreen
import com.mapmory.shared.presentation.triprecord.state.TripRecordDetailUiState
import com.mapmory.shared.presentation.triprecord.state.TripRecordEffect
import com.mapmory.shared.presentation.triprecord.state.TripRecordListUiState
import com.mapmory.shared.presentation.triprecord.viewmodel.TripRecordAction
import com.mapmory.shared.presentation.triprecord.viewmodel.TripRecordsViewModel
import kotlinx.serialization.Serializable

@Serializable
private data object MapRoute

@Serializable
private data object RecordsRoute

@Serializable
private data object CreateRoute

@Serializable
private data object ProfileRoute

@Serializable
private data class DetailRoute(
    val recordId: Long,
)

@Composable
fun MapmoryApp(
    providedRecordsViewModel: TripRecordsViewModel? = null,
    navigation: MapmoryNavigation? = null,
    contentWindowInsets: WindowInsets = WindowInsets(0, 0, 0, 0),
) {
    val navController = rememberNavController()
    val recordsViewModel = remember(providedRecordsViewModel) {
        providedRecordsViewModel ?: TripRecordsViewModel(appLocations)
    }
    val recordsUiState = recordsViewModel.uiState

    fun navigateBack(): Boolean {
        if (navController.currentDestination?.id == navController.graph.startDestinationId) {
            return false
        }
        if (navController.popBackStack()) {
            return true
        }

        // popBackStack can empty the stack when a destination has no parent.
        // Restore the home route instead of leaving NavHost without content.
        navController.navigate(MapRoute) {
            launchSingleTop = true
        }
        return true
    }

    DisposableEffect(navigation, navController) {
        navigation?.bindBackHandler(::navigateBack)
        onDispose { navigation?.unbindBackHandler() }
    }

    var mapScope by remember { mutableStateOf(MapScope.KOREA) }
    var selectedProvinceCode by remember { mutableStateOf<String?>(null) }
    var koreaMapRetryKey by remember { mutableStateOf(0) }
    var koreaMapUiState by remember { mutableStateOf<KoreaMapUiState>(KoreaMapUiState.Idle) }
    val mapHttpClient = remember { createHttpClient() }
    val koreaMapDataSource = remember(mapHttpClient) { KoreaMapRemoteDataSource(mapHttpClient) }
    val locationsById = remember { appLocations.associateBy(Location::id) }

    DisposableEffect(mapHttpClient) {
        onDispose { mapHttpClient.close() }
    }

    LaunchedEffect(mapScope, koreaMapRetryKey) {
        if (mapScope != MapScope.KOREA) return@LaunchedEffect
        koreaMapUiState = KoreaMapUiState.Loading
        koreaMapUiState = koreaMapDataSource.load()
            .fold(
                onSuccess = KoreaMapUiState::Success,
                onFailure = { error ->
                    KoreaMapUiState.Error(error.message ?: "대한민국 지도를 불러오지 못했습니다.")
                },
            )
    }

    fun navigateToTab(route: Any) {
        navController.navigate(route) {
            popUpTo<MapRoute> {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    fun handleMapLocationClick(regionCode: String) {
        val location = appLocations.firstOrNull { it.regionCode == regionCode } ?: return
        recordsViewModel.onAction(TripRecordAction.MapLocationSelected(location))
    }

    fun handleMapDistrictClick(
        regionCode: String,
        regions: List<ProvincePolygon>,
        provinceCode: String?,
    ) {
        val districtLocations = appLocations.filter { location ->
            location.type == LocationType.DISTRICT &&
                locationsById[location.parentId]?.regionCode == provinceCode
        }
        val regionName = regions.firstOrNull { it.code == regionCode }?.name ?: return
        val location = findMapDistrictLocation(regionName, districtLocations)

        location?.let { recordsViewModel.onAction(TripRecordAction.MapLocationSelected(it)) }
    }

    LaunchedEffect(recordsUiState.effect) {
        val effect = recordsUiState.effect ?: return@LaunchedEffect
        when (effect) {
            TripRecordEffect.OpenRecords -> navigateToTab(RecordsRoute)
            TripRecordEffect.OpenEditor -> navController.navigate(CreateRoute)
            is TripRecordEffect.OpenDetail -> {
                val replaced = effect.replaceCurrent && navController.popBackStack()
                if (!replaced) navController.navigate(DetailRoute(effect.recordId))
            }
            TripRecordEffect.CloseDetail -> {
                if (!navController.popBackStack()) navigateToTab(RecordsRoute)
            }
        }
        recordsViewModel.onAction(TripRecordAction.EffectHandled)
    }

    val visitedLocations = recordsUiState.records.mapNotNull { record ->
        appLocations.firstOrNull { it.name == record.locationName }
    }
    val visitedCountryCodes = visitedLocations.map { location ->
        if (location.countryId == 1L) "KR" else location.regionCode
    }.toSet()
    val visitedRegionCodes = visitedLocations.mapNotNull { location ->
        when {
            location.countryId != 1L -> null
            location.type == LocationType.PROVINCE -> location.regionCode
            else -> locationsById[location.parentId]?.regionCode
        }
    }.toSet()
    val selectedProvinceAppCode = selectedProvinceCode
        ?.let(::serverProvinceCodeForGeoJson)
        ?.let { "KR-$it" }
    val selectedProvinceVisitedCount = selectedProvinceAppCode?.let { provinceCode ->
        visitedLocations.count { location ->
            location.type == LocationType.DISTRICT &&
                locationsById[location.parentId]?.regionCode == provinceCode
        }
    }

    NavHost(
        navController = navController,
        startDestination = MapRoute,
    ) {
        composable<MapRoute> {
            TripMapScreen(
                modifier = Modifier.windowInsetsPadding(contentWindowInsets),
                mapScope = mapScope,
                visitedCount = when {
                    mapScope == MapScope.WORLD -> visitedCountryCodes.size
                    selectedProvinceVisitedCount != null -> selectedProvinceVisitedCount
                    else -> visitedRegionCodes.size
                },
                onMapScopeChange = {
                    mapScope = it
                    selectedProvinceCode = null
                },
                mapContent = {
                    when (mapScope) {
                        MapScope.WORLD -> MapArtwork(
                            scope = MapScope.WORLD,
                            visitedCountryCodes = visitedCountryCodes,
                            onCountryClick = ::handleMapLocationClick,
                        )

                        MapScope.KOREA -> when (val state = koreaMapUiState) {
                            KoreaMapUiState.Idle,
                            KoreaMapUiState.Loading,
                            -> KoreaMapStatusMessage("대한민국 지도를 불러오는 중...")

                            is KoreaMapUiState.Error -> KoreaMapStatusMessage(
                                message = state.message,
                                actionLabel = "다시 시도",
                                onAction = { koreaMapRetryKey++ },
                            )

                            is KoreaMapUiState.Success -> {
                                val selectedServerProvinceCode = selectedProvinceCode
                                    ?.let(::serverProvinceCodeForGeoJson)
                                    ?.let { "KR-$it" }
                                val regions = selectedServerProvinceCode?.let(state.data::displayDistrictsFor)
                                    ?: GeneratedKoreaMapData.provinces
                                val visitedCodes = if (selectedServerProvinceCode == null) {
                                    visitedRegionCodes
                                } else {
                                    visitedLocations
                                        .filter { location ->
                                            location.type == LocationType.DISTRICT &&
                                                locationsById[location.parentId]?.regionCode == selectedServerProvinceCode
                                        }
                                        .map { it.regionCode }
                                        .toSet()
                                }

                                MapArtwork(
                                    scope = MapScope.KOREA,
                                    visitedRegionCodes = visitedCodes,
                                    koreaRegions = regions,
                                    showRegionLabels = selectedProvinceCode != null,
                                    onRegionClick = { regionCode ->
                                        if (selectedProvinceCode == null) {
                                            selectedProvinceCode = if (regionCode.startsWith("KR-")) {
                                                geoJsonProvinceCodeForServer(regionCode)
                                            } else {
                                                regionCode
                                            }
                                        } else {
                                            handleMapDistrictClick(
                                                regionCode = regionCode,
                                                regions = regions,
                                                provinceCode = selectedServerProvinceCode,
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                },
                onBackClick = {},
                mapDetailTitle = selectedProvinceCode?.let { code ->
                    (koreaMapUiState as? KoreaMapUiState.Success)
                        ?.data
                        ?.provinces
                        ?.firstOrNull { it.code == code }
                        ?.name
                },
                mapDetailTotal = selectedProvinceCode?.let { code ->
                    (koreaMapUiState as? KoreaMapUiState.Success)
                        ?.data
                        ?.displayDistrictsFor("KR-${serverProvinceCodeForGeoJson(code)}")
                        ?.size
                },
                onMapDetailBackClick = { selectedProvinceCode = null },
                onRecordClick = { navigateToTab(RecordsRoute) },
                onCreateClick = {
                    recordsViewModel.onAction(TripRecordAction.StartCreating())
                },
                onProfileClick = { navigateToTab(ProfileRoute) },
            )
        }

        composable<RecordsRoute> {
            TripRecordListScreen(
                modifier = Modifier.windowInsetsPadding(contentWindowInsets),
                uiState = TripRecordListUiState.Success(
                    records = recordsUiState.visibleRecords,
                    page = 0,
                    totalPages = 1,
                ),
                filter = recordsUiState.filter,
                locations = appLocations,
                onKeywordChanged = { keyword ->
                    recordsViewModel.onAction(TripRecordAction.KeywordChanged(keyword))
                },
                onLocationChanged = { locationId ->
                    recordsViewModel.onAction(TripRecordAction.LocationFilterChanged(locationId))
                },
                onSearchClick = {},
                onPreviousPageClick = {},
                onNextPageClick = {},
                onCreateClick = {
                    recordsViewModel.onAction(TripRecordAction.StartCreating())
                },
                onMapClick = { navigateToTab(MapRoute) },
                onRecordClick = { recordId ->
                    recordsViewModel.onAction(TripRecordAction.RecordSelected(recordId))
                },
                onProfileClick = { navigateToTab(ProfileRoute) },
            )
        }

        composable<CreateRoute> {
            TripRecordEditorScreen(
                modifier = Modifier.windowInsetsPadding(
                    contentWindowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                ),
                uiState = recordsUiState.editor,
                locations = appLocations,
                onLocationSelected = { location ->
                    recordsViewModel.onAction(TripRecordAction.LocationSelected(location))
                },
                onLocationTouched = {
                    recordsViewModel.onAction(TripRecordAction.LocationTouched)
                },
                onTitleChanged = { title ->
                    recordsViewModel.onAction(TripRecordAction.TitleChanged(title))
                },
                onContentChanged = { content ->
                    recordsViewModel.onAction(TripRecordAction.ContentChanged(content))
                },
                onStartDateChanged = { date ->
                    recordsViewModel.onAction(TripRecordAction.StartDateChanged(date))
                },
                onEndDateChanged = { date ->
                    recordsViewModel.onAction(TripRecordAction.EndDateChanged(date))
                },
                onPhotosAdded = { photos ->
                    recordsViewModel.onAction(TripRecordAction.PhotosAdded(photos))
                },
                onPhotoRemoved = { photoId ->
                    recordsViewModel.onAction(TripRecordAction.PhotoRemoved(photoId))
                },
                onSaveClick = { recordsViewModel.onAction(TripRecordAction.Save) },
                onBackClick = { navigateBack() },
                onMapClick = { navigateToTab(MapRoute) },
                onRecordClick = { navigateToTab(RecordsRoute) },
                onProfileClick = { navigateToTab(ProfileRoute) },
            )
        }

        composable<ProfileRoute> {
            TripProfileScreen(
                modifier = Modifier.windowInsetsPadding(contentWindowInsets),
                onMapClick = { navigateToTab(MapRoute) },
                onRecordClick = { navigateToTab(RecordsRoute) },
                onCreateClick = {
                    recordsViewModel.onAction(TripRecordAction.StartCreating())
                },
                onProfileClick = { navigateToTab(ProfileRoute) },
            )
        }

        composable<DetailRoute> { backStackEntry ->
            val detailRoute = backStackEntry.toRoute<DetailRoute>()
            val selectedRecord = recordsUiState.records.firstOrNull { it.id == detailRoute.recordId }
            TripRecordDetailScreen(
                modifier = Modifier.windowInsetsPadding(
                    contentWindowInsets.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                    ),
                ),
                uiState = selectedRecord?.let(TripRecordDetailUiState::Success)
                    ?: TripRecordDetailUiState.Error("여행 기록을 찾을 수 없습니다."),
                onBackClick = { navigateBack() },
                onEditClick = {
                    recordsViewModel.onAction(TripRecordAction.StartEditing(detailRoute.recordId))
                },
                onDeleteClick = {
                    recordsViewModel.onAction(TripRecordAction.Delete(detailRoute.recordId))
                },
                onMapClick = { navigateToTab(MapRoute) },
                onRecordClick = { navigateToTab(RecordsRoute) },
                onCreateClick = {
                    recordsViewModel.onAction(TripRecordAction.StartCreating())
                },
                onProfileClick = { navigateToTab(ProfileRoute) },
            )
        }
    }
}

fun createTripRecordsViewModel(): TripRecordsViewModel = TripRecordsViewModel(appLocations)

internal fun findMapDistrictLocation(
    mapRegionName: String,
    districtLocations: List<Location>,
): Location? {
    val normalizedMapRegionName = normalizeMapRegionName(mapRegionName)
    return districtLocations.singleOrNull { location ->
        normalizeMapRegionName(location.name) == normalizedMapRegionName
    }
}

private fun normalizeMapRegionName(name: String): String {
    val compactName = name.replace(" ", "")
    val provincePrefixes = listOf(
        "서울특별시",
        "부산광역시",
        "대구광역시",
        "인천광역시",
        "광주광역시",
        "대전광역시",
        "울산광역시",
        "세종특별자치시",
        "경기도",
        "강원특별자치도",
        "강원도",
        "충청북도",
        "충청남도",
        "전북특별자치도",
        "전라북도",
        "전라남도",
        "경상북도",
        "경상남도",
        "제주특별자치도",
    )
    return provincePrefixes.firstOrNull(compactName::startsWith)
        ?.let(compactName::removePrefix)
        ?: compactName
}

private val appLocations = buildList {
    add(
        Location(
            id = 1L,
            countryId = 1L,
            parentId = null,
            regionCode = "KR-11",
            name = "서울특별시",
            type = LocationType.PROVINCE,
        ),
    )
    listOf(
        4L to ("KR-26" to "부산광역시"),
        5L to ("KR-27" to "대구광역시"),
        6L to ("KR-28" to "인천광역시"),
        7L to ("KR-29" to "광주광역시"),
        8L to ("KR-30" to "대전광역시"),
        9L to ("KR-31" to "울산광역시"),
        10L to ("KR-50" to "세종특별자치시"),
        11L to ("KR-41" to "경기도"),
        12L to ("KR-42" to "강원특별자치도"),
        13L to ("KR-43" to "충청북도"),
        14L to ("KR-44" to "충청남도"),
        15L to ("KR-45" to "전북특별자치도"),
        16L to ("KR-46" to "전라남도"),
        17L to ("KR-47" to "경상북도"),
        18L to ("KR-48" to "경상남도"),
        19L to ("KR-49" to "제주특별자치도"),
    ).forEach { (id, region) ->
        add(
            Location(
                id = id,
                countryId = 1L,
                parentId = null,
                regionCode = region.first,
                name = region.second,
                type = LocationType.PROVINCE,
            ),
        )
    }

    val koreaProvinceIds = filter {
        it.countryId == 1L && it.type == LocationType.PROVINCE
    }.associate { it.regionCode to it.id }

    KoreanSelectableDistrictCodes.forEachIndexed { index, district ->
        val id = when (district.code) {
            "11650" -> 3L
            "11680" -> 2L
            else -> 20_000L + index
        }
        add(
            Location(
                id = id,
                countryId = 1L,
                parentId = district.provinceCode?.let { koreaProvinceIds[it] },
                regionCode = district.code,
                name = normalizeMapRegionName(district.name),
                type = LocationType.DISTRICT,
            ),
        )
    }

    GeneratedWorldMapData.countries.forEachIndexed { index, country ->
        val id = 10_000L + index
        add(
            Location(
                id = id,
                countryId = id,
                parentId = null,
                regionCode = country.code,
                name = KoreanCountryNames.byCode[country.code] ?: country.name,
                type = LocationType.PROVINCE,
            ),
        )
    }
}
