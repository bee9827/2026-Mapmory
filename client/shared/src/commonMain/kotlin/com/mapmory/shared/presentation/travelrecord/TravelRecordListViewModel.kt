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

    suspend fun load(query: TravelRecordQuery = TravelRecordQuery()) {
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
