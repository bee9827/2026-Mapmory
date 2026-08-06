package com.mapmory.shared.presentation.travelrecord

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mapmory.shared.domain.model.TravelRecord
import com.mapmory.shared.domain.model.TravelRecordQuery
import com.mapmory.shared.domain.usecase.GetTravelRecordsUseCase

/** 여행 기록 목록 화면의 상태와 조회 동작을 관리한다. */
class TravelRecordListViewModel(
    private val getTravelRecords: GetTravelRecordsUseCase,
) {
    var uiState by mutableStateOf<TravelRecordListUiState>(TravelRecordListUiState.Idle)
        private set

    var query by mutableStateOf(TravelRecordQuery())
        private set

    fun updateKeyword(keyword: String) {
        query = query.copy(keyword = keyword.ifBlank { null }, page = 0)
    }

    fun filterByLocation(locationId: Long?) {
        query = query.copy(locationId = locationId, page = 0)
    }

    suspend fun load(query: TravelRecordQuery = this.query) {
        this.query = query
        uiState = TravelRecordListUiState.Loading
        uiState = getTravelRecords(query).fold(
            onSuccess = { page ->
                TravelRecordListUiState.Success(
                    records = page.records,
                    page = page.page,
                    totalPages = page.totalPages,
                )
            },
            onFailure = { error ->
                TravelRecordListUiState.Error(error.message ?: "여행 기록을 불러오지 못했습니다.")
            },
        )
    }

    suspend fun previousPage() {
        if (query.page > 0) load(query.copy(page = query.page - 1))
    }

    suspend fun nextPage() {
        val state = uiState as? TravelRecordListUiState.Success ?: return
        if (query.page + 1 < state.totalPages) load(query.copy(page = query.page + 1))
    }
}

sealed interface TravelRecordListUiState {
    data object Idle : TravelRecordListUiState

    data object Loading : TravelRecordListUiState

    data class Success(
        val records: List<TravelRecord>,
        val page: Int,
        val totalPages: Int,
    ) : TravelRecordListUiState

    data class Error(
        val message: String,
    ) : TravelRecordListUiState
}
