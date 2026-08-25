package com.mapmory.shared.presentation.triprecord.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mapmory.shared.domain.model.TripRecordQuery
import com.mapmory.shared.domain.region.RegionCatalog
import com.mapmory.shared.domain.usecase.GetTripRecordsUseCase
import com.mapmory.shared.presentation.triprecord.state.TripRecordListUiState
import com.mapmory.shared.presentation.triprecord.state.toTripRecordItemUiState

/** 여행 기록 목록 화면의 상태와 조회 동작을 관리한다. */
class TripRecordListViewModel(
    private val getTripRecords: GetTripRecordsUseCase,
    private val regionCatalog: RegionCatalog? = null,
) : ViewModel() {
    private var isRouteInitialized = false

    var uiState by mutableStateOf<TripRecordListUiState>(TripRecordListUiState.Idle)
        private set

    var query by mutableStateOf(TripRecordQuery())
        private set

    fun updateKeyword(keyword: String) {
        query = query.copy(keyword = keyword.ifBlank { null }, page = 0)
    }

    fun filterByLocation(locationId: Long?) {
        query = query.copy(locationId = locationId, page = 0)
    }

    suspend fun initialize(locationId: Long?) {
        if (isRouteInitialized) return
        isRouteInitialized = true
        filterByLocation(locationId)
        load()
    }

    suspend fun load(query: TripRecordQuery = this.query) {
        this.query = query
        uiState = TripRecordListUiState.Loading
        uiState = getTripRecords(query).fold(
            onSuccess = { page ->
                TripRecordListUiState.Success(
                    records = page.records.map { record ->
                        record.toTripRecordItemUiState(
                            locationName = regionCatalog?.findById(record.locationId)?.name ?: "여행지",
                        )
                    },
                    page = page.page,
                    totalPages = page.totalPages,
                )
            },
            onFailure = { error ->
                TripRecordListUiState.Error(error.message ?: "여행 기록을 불러오지 못했습니다.")
            },
        )
    }

    suspend fun previousPage() {
        if (query.page > 0) load(query.copy(page = query.page - 1))
    }

    suspend fun nextPage() {
        val state = uiState as? TripRecordListUiState.Success ?: return
        if (query.page + 1 < state.totalPages) load(query.copy(page = query.page + 1))
    }
}
