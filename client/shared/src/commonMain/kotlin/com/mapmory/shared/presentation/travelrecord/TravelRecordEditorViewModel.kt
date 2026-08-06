package com.mapmory.shared.presentation.travelrecord

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.TravelRecord
import com.mapmory.shared.domain.model.TravelRecordDraft
import com.mapmory.shared.domain.usecase.CreateTravelRecordUseCase
import com.mapmory.shared.domain.usecase.UpdateTravelRecordUseCase

class TravelRecordEditorViewModel(
    private val createTravelRecord: CreateTravelRecordUseCase,
    private val updateTravelRecord: UpdateTravelRecordUseCase,
) {
    var uiState by mutableStateOf(TravelRecordEditorUiState())
        private set

    fun reset() {
        uiState = TravelRecordEditorUiState()
    }

    fun startEditing(record: TravelRecord, location: Location) {
        uiState = TravelRecordEditorUiState(
            recordId = record.id,
            selectedLocation = location,
            title = record.title,
            content = record.content,
            startDate = record.startDate.orEmpty(),
            endDate = record.endDate.orEmpty(),
            mediaObjectKeys = record.media.map { it.objectKey },
        )
    }

    fun selectLocation(location: Location) {
        uiState = uiState.copy(selectedLocation = location, errorMessage = null)
    }

    fun updateTitle(title: String) {
        uiState = uiState.copy(title = title, errorMessage = null)
    }

    fun updateContent(content: String) {
        uiState = uiState.copy(content = content, errorMessage = null)
    }

    fun updateStartDate(startDate: String) {
        uiState = uiState.copy(startDate = startDate, errorMessage = null)
    }

    fun updateEndDate(endDate: String) {
        uiState = uiState.copy(endDate = endDate, errorMessage = null)
    }

    suspend fun save(): Boolean {
        val state = uiState
        val location = state.selectedLocation ?: return fail("장소를 선택해 주세요.")
        if (state.title.isBlank()) return fail("제목을 입력해 주세요.")

        uiState = state.copy(isSaving = true, errorMessage = null)
        val draft = TravelRecordDraft(
            locationId = location.id,
            title = state.title.trim(),
            content = state.content.trim(),
            startDate = state.startDate.ifBlank { null },
            endDate = state.endDate.ifBlank { null },
            mediaObjectKeys = state.mediaObjectKeys,
        )
        val result = state.recordId?.let { updateTravelRecord(it, draft) }
            ?: createTravelRecord(draft)

        return result.fold(
            onSuccess = {
                uiState = uiState.copy(isSaving = false)
                true
            },
            onFailure = { error ->
                uiState = uiState.copy(
                    isSaving = false,
                    errorMessage = error.message ?: "여행 기록을 저장하지 못했습니다.",
                )
                false
            },
        )
    }

    private fun fail(message: String): Boolean {
        uiState = uiState.copy(errorMessage = message)
        return false
    }
}

data class TravelRecordEditorUiState(
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
