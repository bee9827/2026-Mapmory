package com.mapmory.shared.presentation.triprecord.state

import com.mapmory.shared.domain.model.TripRecordData

sealed interface TripRecordListUiState {
    data object Idle : TripRecordListUiState

    data object Loading : TripRecordListUiState

    data class Success(
        val records: List<TripRecordData>,
        val page: Int,
        val totalPages: Int,
    ) : TripRecordListUiState

    data class Error(
        val message: String,
    ) : TripRecordListUiState
}
