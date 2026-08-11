package com.mapmory.shared.presentation.triprecord.state

import com.mapmory.shared.domain.model.Location

data class TripRecordEditorUiState(
    val recordId: Long? = null,
    val selectedLocation: Location? = null,
    val title: String = "",
    val content: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val mediaObjectKeys: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)
