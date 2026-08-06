package com.mapmory.shared.presentation.travelrecord

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mapmory.shared.domain.model.TravelRecord
import com.mapmory.shared.domain.usecase.GetTravelRecordUseCase

class TravelRecordDetailViewModel(
    private val getTravelRecord: GetTravelRecordUseCase,
) {
    var uiState by mutableStateOf<TravelRecordDetailUiState>(TravelRecordDetailUiState.Idle)
        private set

    suspend fun load(id: Long) {
        uiState = TravelRecordDetailUiState.Loading
        uiState = getTravelRecord(id).fold(
            onSuccess = TravelRecordDetailUiState::Success,
            onFailure = { error ->
                TravelRecordDetailUiState.Error(error.message ?: "여행 기록을 불러오지 못했습니다.")
            },
        )
    }
}

sealed interface TravelRecordDetailUiState {
    data object Idle : TravelRecordDetailUiState

    data object Loading : TravelRecordDetailUiState

    data class Success(
        val record: TravelRecord,
    ) : TravelRecordDetailUiState

    data class Error(
        val message: String,
    ) : TravelRecordDetailUiState
}
