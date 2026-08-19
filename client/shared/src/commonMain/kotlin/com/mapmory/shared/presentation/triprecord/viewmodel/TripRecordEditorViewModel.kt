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
import com.mapmory.shared.presentation.triprecord.state.TripRecordEditorErrorTarget
import com.mapmory.shared.presentation.triprecord.state.TripRecordEditorUiState
import com.mapmory.shared.presentation.triprecord.state.toTripRecordPhotoUiState

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
                ).toTripRecordPhotoUiState(media.sortOrder)
            },
        )
    }

    fun selectLocation(location: Location) {
        uiState = uiState.copy(
            selectedLocation = location,
        ).revalidatedAfterChange(TripRecordEditorErrorTarget.LOCATION)
    }

    fun touchLocation() {
        uiState = uiState.revalidatedAfterChange(TripRecordEditorErrorTarget.LOCATION)
    }

    fun clearLocation() {
        uiState = uiState.copy(
            selectedLocation = null,
        ).revalidatedAfterChange(TripRecordEditorErrorTarget.LOCATION)
    }

    fun updateTitle(title: String) {
        uiState = uiState.copy(
            title = title,
        ).revalidatedAfterChange(TripRecordEditorErrorTarget.TITLE)
    }

    fun updateContent(content: String) {
        uiState = uiState.copy(
            content = content,
        ).revalidatedAfterChange()
    }

    fun updateStartDate(startDate: String) {
        uiState = uiState.copy(
            startDate = startDate,
        ).revalidatedAfterChange(TripRecordEditorErrorTarget.START_DATE)
    }

    fun updateEndDate(endDate: String) {
        uiState = uiState.copy(
            endDate = endDate,
        ).revalidatedAfterChange(TripRecordEditorErrorTarget.END_DATE)
    }

    fun addMediaObjectKey(objectKey: String) {
        val trimmedObjectKey = objectKey.trim()
        if (trimmedObjectKey.isBlank() || trimmedObjectKey in uiState.mediaObjectKeys) return

        uiState = uiState.copy(
            mediaObjectKeys = uiState.mediaObjectKeys + trimmedObjectKey,
        ).revalidatedAfterChange()
    }

    fun removeMediaObjectKey(objectKey: String) {
        uiState = uiState.copy(
            mediaObjectKeys = uiState.mediaObjectKeys - objectKey,
            selectedPhotos = uiState.selectedPhotos.filterNot { it.id == objectKey },
            fieldErrors = uiState.fieldErrors - TripRecordEditorErrorTarget.PHOTOS,
        ).revalidatedAfterChange()
    }

    suspend fun save(): Boolean {
        val state = uiState
        val validationErrors = state.validationErrors()
        if (validationErrors.isNotEmpty()) return fail(validationErrors)

        val location = requireNotNull(state.selectedLocation)

        val draft = TripRecordDraft(
            locationId = location.id,
            title = state.title.trim(),
            content = state.content.trim(),
            startDate = state.startDate.ifBlank { null },
            endDate = state.endDate.ifBlank { null },
            mediaObjectKeys = state.mediaObjectKeys,
        )
        uiState = state.copy(isSaving = true, fieldErrors = emptyMap(), generalErrorMessage = null)
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
                    generalErrorMessage = error.message ?: "여행 기록을 저장하지 못했습니다.",
                )
                false
            },
        )
    }

    private fun fail(errors: Map<TripRecordEditorErrorTarget, String>): Boolean {
        uiState = uiState.copy(
            isDirty = true,
            dirtyFields = uiState.dirtyFields + errors.keys,
            fieldErrors = errors,
            generalErrorMessage = null,
        )
        return false
    }
}

private fun TripRecordEditorUiState.revalidatedAfterChange(
    dirtyTarget: TripRecordEditorErrorTarget? = null,
): TripRecordEditorUiState {
    if (dirtyTarget == null) {
        return copy(isDirty = true, generalErrorMessage = null)
    }

    val updatedDirtyFields = if (dirtyTarget in dirtyFields) dirtyFields else dirtyFields + dirtyTarget
    val nonValidationErrors = fieldErrors.filterKeys { target ->
        target == TripRecordEditorErrorTarget.PHOTOS
    }
    val dateRangeErrorTarget = when (dirtyTarget) {
        TripRecordEditorErrorTarget.START_DATE,
        TripRecordEditorErrorTarget.END_DATE -> dirtyTarget

        else -> fieldErrors.keys.firstOrNull { target ->
            target == TripRecordEditorErrorTarget.START_DATE ||
                target == TripRecordEditorErrorTarget.END_DATE
        } ?: TripRecordEditorErrorTarget.END_DATE
    }
    return copy(
        isDirty = true,
        dirtyFields = updatedDirtyFields,
        fieldErrors = nonValidationErrors + validationErrors(dateRangeErrorTarget)
            .filterKeys(updatedDirtyFields::contains),
        generalErrorMessage = null,
    )
}

private fun TripRecordEditorUiState.validationErrors(
    dateRangeErrorTarget: TripRecordEditorErrorTarget = TripRecordEditorErrorTarget.END_DATE,
): Map<TripRecordEditorErrorTarget, String> = buildMap {
    if (selectedLocation == null) {
        put(TripRecordEditorErrorTarget.LOCATION, "장소를 선택해 주세요.")
    }
    if (title.isBlank()) {
        put(TripRecordEditorErrorTarget.TITLE, "제목을 입력해 주세요.")
    }

    val dateError = TripRecordDraft(
        locationId = selectedLocation?.id ?: 0L,
        title = title,
        content = content,
        startDate = startDate.ifBlank { null },
        endDate = endDate.ifBlank { null },
        mediaObjectKeys = mediaObjectKeys,
    ).dateValidationError()
    if (dateError != null) {
        val target = when (dateError) {
            "올바른 시작일을 입력해 주세요." -> TripRecordEditorErrorTarget.START_DATE
            "올바른 종료일을 입력해 주세요." -> TripRecordEditorErrorTarget.END_DATE
            else -> dateRangeErrorTarget
        }
        put(target, dateError)
    }
}
