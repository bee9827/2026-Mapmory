package com.mapmory.shared.presentation.triprecord.state

import androidx.compose.runtime.Immutable

@Immutable
sealed interface TripRecordDetailUiState {
    data object Idle : TripRecordDetailUiState

    data object Loading : TripRecordDetailUiState

    data object Deleting : TripRecordDetailUiState

    data class Success(
        val record: TripRecordItemUiState,
    ) : TripRecordDetailUiState

    data class Error(
        val message: String,
    ) : TripRecordDetailUiState
}
