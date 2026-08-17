package com.mapmory.shared.presentation.triprecord.state

import androidx.compose.runtime.Immutable

@Immutable
sealed interface TripRecordListUiState {
    data object Idle : TripRecordListUiState

    data object Loading : TripRecordListUiState

    data class Success(
        val records: List<TripRecordItemUiState>,
        val page: Int,
        val totalPages: Int,
    ) : TripRecordListUiState

    data class Error(
        val message: String,
    ) : TripRecordListUiState
}
