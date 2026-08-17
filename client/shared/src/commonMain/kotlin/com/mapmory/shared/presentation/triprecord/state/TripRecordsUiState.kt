package com.mapmory.shared.presentation.triprecord.state

data class TripRecordFilterUiState(
    val locationId: Long? = null,
    val keyword: String = "",
)

data class TripRecordsUiState(
    val records: List<TripRecordItemUiState> = emptyList(),
    val visibleRecords: List<TripRecordItemUiState> = emptyList(),
    val filter: TripRecordFilterUiState = TripRecordFilterUiState(),
    val editor: TripRecordEditorUiState = TripRecordEditorUiState(),
    val effect: TripRecordEffect? = null,
)

sealed interface TripRecordEffect {
    data object OpenRecords : TripRecordEffect

    data object OpenEditor : TripRecordEffect

    data class OpenDetail(
        val recordId: Long,
        val replaceCurrent: Boolean = false,
    ) : TripRecordEffect

    data object CloseDetail : TripRecordEffect
}
