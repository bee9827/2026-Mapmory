package com.mapmory.shared.presentation.triprecord.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.TripRecordData
import com.mapmory.shared.domain.model.TripRecordDraft
import com.mapmory.shared.domain.model.dateValidationError
import com.mapmory.shared.domain.usecase.CreateTripRecordUseCase
import com.mapmory.shared.domain.usecase.UpdateTripRecordUseCase
import com.mapmory.shared.presentation.photo.SelectedPhoto
import com.mapmory.shared.presentation.triprecord.state.TripRecordEditorUiState

class TripRecordEditorViewModel(
    private val createTripRecord: CreateTripRecordUseCase,
    private val updateTripRecord: UpdateTripRecordUseCase,
) {
    var uiState by mutableStateOf(TripRecordEditorUiState())
        private set

    fun reset() {
        uiState = TripRecordEditorUiState()
    }

    fun startEditing(record: TripRecordData, location: Location) {
        uiState = TripRecordEditorUiState(
            recordId = record.id,
            selectedLocation = location,
            title = record.title,
            content = record.content,
            startDate = record.startDate.orEmpty(),
            endDate = record.endDate.orEmpty(),
            mediaObjectKeys = record.media.map { it.objectKey },
            selectedPhotos = record.media.map { media ->
                SelectedPhoto(
                    id = media.objectKey,
                    displayName = media.objectKey.substringAfterLast('/'),
                    previewBytes = media.previewBytes,
                )
            },
        )
    }

    fun selectLocation(location: Location) {
        uiState = uiState.copy(selectedLocation = location, errorMessage = null)
    }

    fun clearLocation() {
        uiState = uiState.copy(selectedLocation = null, errorMessage = null)
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

    fun addMediaObjectKey(objectKey: String) {
        val trimmedObjectKey = objectKey.trim()
        if (trimmedObjectKey.isBlank() || trimmedObjectKey in uiState.mediaObjectKeys) return

        uiState = uiState.copy(mediaObjectKeys = uiState.mediaObjectKeys + trimmedObjectKey)
    }

    fun removeMediaObjectKey(objectKey: String) {
        uiState = uiState.copy(
            mediaObjectKeys = uiState.mediaObjectKeys - objectKey,
            selectedPhotos = uiState.selectedPhotos.filterNot { it.id == objectKey },
        )
    }

    suspend fun save(): Boolean {
        val state = uiState
        val location = state.selectedLocation ?: return fail("장소를 선택해 주세요.")
        if (state.title.isBlank()) return fail("제목을 입력해 주세요.")

        val draft = TripRecordDraft(
            locationId = location.id,
            title = state.title.trim(),
            content = state.content.trim(),
            startDate = state.startDate.ifBlank { null },
            endDate = state.endDate.ifBlank { null },
            mediaObjectKeys = state.mediaObjectKeys,
        )
        draft.dateValidationError()?.let { return fail(it) }

        uiState = state.copy(isSaving = true, errorMessage = null)
        val result = state.recordId?.let { updateTripRecord(it, draft) }
            ?: createTripRecord(draft)

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
