package com.mapmory.shared.presentation.triprecord.state

import com.mapmory.shared.domain.model.TripRecordData

sealed interface TripRecordDetailUiState {
    data object Idle : TripRecordDetailUiState

    data object Loading : TripRecordDetailUiState

    data object Deleting : TripRecordDetailUiState

    data class Success(
        val record: TripRecordData,
    ) : TripRecordDetailUiState

    data class Error(
        val message: String,
    ) : TripRecordDetailUiState
}
