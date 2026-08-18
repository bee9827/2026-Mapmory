package com.mapmory.shared.presentation.triprecord.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mapmory.shared.domain.TripRecords
import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.LocationType
import com.mapmory.shared.presentation.photo.MaxPhotosPerRecord
import com.mapmory.shared.presentation.photo.SelectedPhoto
import com.mapmory.shared.presentation.triprecord.state.TripRecordEditorUiState
import com.mapmory.shared.presentation.triprecord.state.TripRecordEffect
import com.mapmory.shared.presentation.triprecord.state.TripRecordFilterUiState
import com.mapmory.shared.presentation.triprecord.state.TripRecordItemUiState
import com.mapmory.shared.presentation.triprecord.state.TripRecordPhotoUiState
import com.mapmory.shared.presentation.triprecord.state.TripRecordsUiState
import com.mapmory.shared.presentation.triprecord.state.toTripRecordItemUiState
import com.mapmory.shared.presentation.triprecord.state.toTripRecordPhotoUiState
import kotlinx.datetime.LocalDate

sealed interface TripRecordAction {
    data class KeywordChanged(val keyword: String) : TripRecordAction

    data class LocationFilterChanged(val locationId: Long?) : TripRecordAction

    data class MapLocationSelected(val location: Location) : TripRecordAction

    data class RecordSelected(val recordId: Long) : TripRecordAction

    data class StartCreating(val selectedLocation: Location? = null) : TripRecordAction

    data class StartEditing(val recordId: Long) : TripRecordAction

    data class LocationSelected(val location: Location) : TripRecordAction

    data class TitleChanged(val title: String) : TripRecordAction

    data class ContentChanged(val content: String) : TripRecordAction

    data class StartDateChanged(val date: String) : TripRecordAction

    data class EndDateChanged(val date: String) : TripRecordAction

    data class PhotosAdded(val photos: List<SelectedPhoto>) : TripRecordAction

    data class PhotoRemoved(val photoId: String) : TripRecordAction

    data object Save : TripRecordAction

    data class Delete(val recordId: Long) : TripRecordAction

    data object EffectHandled : TripRecordAction
}

/**
 * 여행 기록 도메인 컬렉션을 단일 원본으로 관리한다.
 * UI는 [uiState]만 읽고 모든 변경은 [onAction]으로 전달한다.
 */
class TripRecordsViewModel(
    locations: List<Location>,
    initialRecords: TripRecords = TripRecords(),
) {
    private val locations = locations.toList()
    private val locationsById = this.locations.associateBy(Location::id)
    private var domainRecords = initialRecords
    private var photosByRecordId: Map<Long, List<TripRecordPhotoUiState>> = initialRecords.tripRecords
        .mapNotNull { record ->
            record.imageUrl.takeIf(String::isNotBlank)?.let { imageUrl ->
                record.id to listOf(
                    TripRecordPhotoUiState(
                        id = imageUrl,
                        displayName = imageUrl.substringAfterLast('/'),
                        previewBytes = null,
                        sortOrder = 0,
                    ),
                )
            }
        }
        .toMap()

    var uiState by mutableStateOf(TripRecordsUiState())
        private set

    init {
        publishRecords()
    }

    fun onAction(action: TripRecordAction) {
        when (action) {
            is TripRecordAction.KeywordChanged -> updateFilter(
                uiState.filter.copy(keyword = action.keyword),
            )

            is TripRecordAction.LocationFilterChanged -> updateFilter(
                uiState.filter.copy(locationId = action.locationId),
            )

            is TripRecordAction.MapLocationSelected -> selectMapLocation(action.location)
            is TripRecordAction.RecordSelected -> emit(TripRecordEffect.OpenDetail(action.recordId))
            is TripRecordAction.StartCreating -> startCreating(action.selectedLocation)
            is TripRecordAction.StartEditing -> startEditing(action.recordId)
            is TripRecordAction.LocationSelected -> updateEditor {
                copy(selectedLocation = action.location, errorMessage = null)
            }

            is TripRecordAction.TitleChanged -> updateEditor {
                copy(title = action.title, errorMessage = null)
            }

            is TripRecordAction.ContentChanged -> updateEditor {
                copy(content = action.content, errorMessage = null)
            }

            is TripRecordAction.StartDateChanged -> updateEditor {
                copy(startDate = action.date, errorMessage = null)
            }

            is TripRecordAction.EndDateChanged -> updateEditor {
                copy(endDate = action.date, errorMessage = null)
            }

            is TripRecordAction.PhotosAdded -> addPhotos(action.photos)
            is TripRecordAction.PhotoRemoved -> removePhoto(action.photoId)
            TripRecordAction.Save -> save()
            is TripRecordAction.Delete -> delete(action.recordId)
            TripRecordAction.EffectHandled -> uiState = uiState.copy(effect = null)
        }
    }

    private fun selectMapLocation(location: Location) {
        if (domainRecords.tripRecords.any { record -> locationContains(location, record.location) }) {
            updateFilter(TripRecordFilterUiState(locationId = location.id))
            emit(TripRecordEffect.OpenRecords)
        } else {
            startCreating(location)
        }
    }

    private fun startCreating(selectedLocation: Location?) {
        uiState = uiState.copy(
            editor = TripRecordEditorUiState(selectedLocation = selectedLocation),
            effect = TripRecordEffect.OpenEditor,
        )
    }

    private fun startEditing(recordId: Long) {
        val record = domainRecords.tripRecords.firstOrNull { it.id == recordId } ?: return
        val photos = photosByRecordId[record.id].orEmpty()
        uiState = uiState.copy(
            editor = TripRecordEditorUiState(
                recordId = record.id,
                selectedLocation = locations.firstOrNull { it.name == record.location },
                title = record.tripRecordTitle,
                content = record.tripRecordDescription.orEmpty(),
                startDate = record.startTripDate.toString(),
                endDate = record.endTripDate.toString(),
                mediaObjectKeys = photos.map(TripRecordPhotoUiState::id),
                selectedPhotos = photos,
            ),
            effect = TripRecordEffect.OpenEditor,
        )
    }

    private fun addPhotos(incoming: List<SelectedPhoto>) {
        val editor = uiState.editor
        val requested = (editor.selectedPhotos.map(TripRecordPhotoUiState::id) + incoming.map(SelectedPhoto::id))
            .distinct()
        val merged = buildList {
            addAll(editor.selectedPhotos)
            incoming.forEach { photo ->
                if (none { it.id == photo.id }) {
                    add(photo.toTripRecordPhotoUiState(sortOrder = size))
                }
            }
        }.take(MaxPhotosPerRecord)

        uiState = uiState.copy(
            editor = editor.copy(
                selectedPhotos = merged,
                mediaObjectKeys = merged.map(TripRecordPhotoUiState::id),
                errorMessage = if (merged.size < requested.size) {
                    "사진은 최대 ${MaxPhotosPerRecord}장까지 추가할 수 있어요."
                } else {
                    null
                },
            ),
        )
    }

    private fun removePhoto(photoId: String) {
        val remaining = uiState.editor.selectedPhotos
            .filterNot { it.id == photoId }
            .mapIndexed { index, photo -> photo.copy(sortOrder = index) }
        updateEditor {
            copy(
                selectedPhotos = remaining,
                mediaObjectKeys = remaining.map(TripRecordPhotoUiState::id),
                errorMessage = null,
            )
        }
    }

    private fun save() {
        val editor = uiState.editor
        val location = editor.selectedLocation ?: return fail("장소를 선택해 주세요.")
        val title = editor.title.trim()
        if (title.isEmpty()) return fail("제목을 입력해 주세요.")

        val startDate = editor.startDate.toLocalDateOrNull()
            ?: return fail("올바른 시작일을 입력해 주세요.")
        val endDate = if (editor.endDate.isBlank()) {
            startDate
        } else {
            editor.endDate.toLocalDateOrNull()
                ?: return fail("올바른 종료일을 입력해 주세요.")
        }
        if (startDate > endDate) return fail("종료일은 시작일보다 빠를 수 없습니다.")

        uiState = uiState.copy(editor = editor.copy(isSaving = true, errorMessage = null))
        val photos = editor.selectedPhotos.mapIndexed { index, photo -> photo.copy(sortOrder = index) }
        val imageUrl = photos.firstOrNull()?.id.orEmpty()

        val savedRecord = runCatching {
            editor.recordId?.let { recordId ->
                val editingRecord = domainRecords.tripRecords.firstOrNull { it.id == recordId }
                    ?: error("수정할 여행 기록을 찾을 수 없습니다.")
                domainRecords = domainRecords.editTripRecord(
                    editingRecord = editingRecord,
                    editingImage = imageUrl,
                    editingTitle = title,
                    editingDescription = editor.content.trim(),
                    editingStartTripDate = startDate,
                    editingEndTripDate = endDate,
                    editingLocation = location.name,
                )
                domainRecords.tripRecords.first { it.id == recordId }
            } ?: run {
                domainRecords = domainRecords.addTripRecord(
                    imageUri = imageUrl,
                    tripRecordTitle = title,
                    tripRecordDescription = editor.content.trim().ifBlank { null },
                    tripLocation = location.name,
                    startTripDate = startDate,
                    endTripDate = endDate,
                )
                domainRecords.tripRecords.last()
            }
        }.getOrElse { error ->
            fail(error.message ?: "여행 기록을 저장하지 못했습니다.")
            return
        }

        photosByRecordId = photosByRecordId + (savedRecord.id to photos)
        val wasEditing = editor.recordId != null
        publishRecords(
            editor = editor.copy(isSaving = false, errorMessage = null),
            filter = TripRecordFilterUiState(),
            effect = if (wasEditing) {
                TripRecordEffect.OpenDetail(savedRecord.id, replaceCurrent = true)
            } else {
                TripRecordEffect.OpenRecords
            },
        )
    }

    private fun delete(recordId: Long) {
        val record = domainRecords.tripRecords.firstOrNull { it.id == recordId } ?: return
        domainRecords = domainRecords.removeTripRecord(record)
        photosByRecordId = photosByRecordId - recordId
        publishRecords(effect = TripRecordEffect.CloseDetail)
    }

    private fun updateFilter(filter: TripRecordFilterUiState) {
        publishRecords(filter = filter)
    }

    private fun updateEditor(transform: TripRecordEditorUiState.() -> TripRecordEditorUiState) {
        uiState = uiState.copy(editor = uiState.editor.transform())
    }

    private fun fail(message: String) {
        updateEditor { copy(isSaving = false, errorMessage = message) }
    }

    private fun emit(effect: TripRecordEffect) {
        uiState = uiState.copy(effect = effect)
    }

    private fun publishRecords(
        editor: TripRecordEditorUiState = uiState.editor,
        filter: TripRecordFilterUiState = uiState.filter,
        effect: TripRecordEffect? = uiState.effect,
    ) {
        val records = domainRecords.tripRecords.map { record ->
            record.toTripRecordItemUiState(photosByRecordId[record.id].orEmpty())
        }
        uiState = TripRecordsUiState(
            records = records,
            visibleRecords = records.filter { record -> record.matches(filter) },
            filter = filter,
            editor = editor,
            effect = effect,
        )
    }

    private fun TripRecordItemUiState.matches(
        filter: TripRecordFilterUiState,
    ): Boolean {
        val selectedLocation = filter.locationId?.let(locationsById::get)
        val matchesLocation = selectedLocation == null || locationContains(selectedLocation, locationName)
        val matchesKeyword = filter.keyword.isBlank() ||
            title.contains(filter.keyword, ignoreCase = true) ||
            content.contains(filter.keyword, ignoreCase = true)
        return matchesLocation && matchesKeyword
    }

    private fun locationContains(selected: Location, recordLocationName: String): Boolean {
        val recordLocation = locations.firstOrNull { it.name == recordLocationName } ?: return false
        return when {
            selected.regionCode == "KR" ->
                recordLocation.countryId == KoreaCountryId || recordLocation.regionCode == "KR"

            selected.countryId == KoreaCountryId && selected.type == LocationType.PROVINCE ->
                recordLocation.id == selected.id || recordLocation.parentId == selected.id

            else -> recordLocation.id == selected.id
        }
    }
}

private fun String.toLocalDateOrNull(): LocalDate? = runCatching {
    LocalDate.parse(trim().replace(" ", "").replace('.', '-'))
}.getOrNull()

private const val KoreaCountryId = 1L
