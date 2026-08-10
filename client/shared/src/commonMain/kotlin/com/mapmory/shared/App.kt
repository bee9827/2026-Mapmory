package com.mapmory.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.LocationType
import com.mapmory.shared.domain.model.TripRecordData
import com.mapmory.shared.domain.model.TripRecordQuery
import com.mapmory.shared.presentation.triprecord.screen.TripMapArtwork
import com.mapmory.shared.presentation.triprecord.screen.TripMapScreen
import com.mapmory.shared.presentation.triprecord.screen.TripProfileScreen
import com.mapmory.shared.presentation.triprecord.screen.TripRecordDetailScreen
import com.mapmory.shared.presentation.triprecord.screen.TripRecordEditorScreen
import com.mapmory.shared.presentation.triprecord.screen.TripRecordListScreen
import com.mapmory.shared.presentation.triprecord.state.TripRecordDetailUiState
import com.mapmory.shared.presentation.triprecord.state.TripRecordEditorUiState
import com.mapmory.shared.presentation.triprecord.state.TripRecordListUiState

private enum class AppDestination {
    MAP,
    RECORDS,
    CREATE,
    PROFILE,
    DETAIL,
}

@Composable
fun MapmoryApp() {
    var destination by remember { mutableStateOf(AppDestination.MAP) }
    var editorReturnDestination by remember { mutableStateOf(AppDestination.MAP) }
    var selectedRecordId by remember { mutableStateOf<Long?>(null) }
    var records by remember { mutableStateOf(emptyList<TripRecordData>()) }
    var query by remember { mutableStateOf(TripRecordQuery()) }
    var editorState by remember {
        mutableStateOf(
            TripRecordEditorUiState(
                selectedLocation = appLocations.firstOrNull { it.type == LocationType.DISTRICT },
            ),
        )
    }

    fun openCreateScreen(returnTo: AppDestination) {
        editorReturnDestination = returnTo
        editorState = TripRecordEditorUiState(
            selectedLocation = appLocations.firstOrNull { it.type == LocationType.DISTRICT },
        )
        destination = AppDestination.CREATE
    }

    fun openEditScreen(record: TripRecordData) {
        val location = appLocations.firstOrNull { it.id == record.locationId }
            ?: appLocations.firstOrNull { it.type == LocationType.DISTRICT }
        editorReturnDestination = AppDestination.DETAIL
        editorState = TripRecordEditorUiState(
            recordId = record.id,
            selectedLocation = location,
            title = record.title,
            content = record.content,
            startDate = record.startDate.orEmpty(),
            endDate = record.endDate.orEmpty(),
        )
        destination = AppDestination.CREATE
    }

    fun saveEditor() {
        val state = editorState
        val location = state.selectedLocation
        when {
            location == null -> editorState = state.copy(errorMessage = "장소를 선택해 주세요.")
            state.title.isBlank() -> editorState = state.copy(errorMessage = "제목을 입력해 주세요.")
            else -> {
                val previousRecord = records.firstOrNull { it.id == state.recordId }
                val record = TripRecordData(
                    id = previousRecord?.id ?: ((records.maxOfOrNull(TripRecordData::id) ?: 0L) + 1L),
                    memberId = 1L,
                    locationId = location.id,
                    title = state.title.trim(),
                    content = state.content.trim(),
                    startDate = state.startDate.ifBlank { null },
                    endDate = state.endDate.ifBlank { null },
                    media = previousRecord?.media.orEmpty(),
                    createdAt = previousRecord?.createdAt.orEmpty(),
                    updatedAt = previousRecord?.updatedAt.orEmpty(),
                )
                records = if (previousRecord == null) {
                    records + record
                } else {
                    records.map { if (it.id == record.id) record else it }
                }
                selectedRecordId = record.id
                query = TripRecordQuery()
                destination = if (state.recordId == null) {
                    AppDestination.RECORDS
                } else {
                    AppDestination.DETAIL
                }
            }
        }
    }

    val visibleRecords = records.filter { record ->
        (query.locationId == null || record.locationId == query.locationId) &&
            (query.keyword.isNullOrBlank() ||
                record.title.contains(query.keyword.orEmpty(), ignoreCase = true) ||
                record.content.contains(query.keyword.orEmpty(), ignoreCase = true))
    }
    val selectedRecord = records.firstOrNull { it.id == selectedRecordId }

    when (destination) {
        AppDestination.MAP -> TripMapScreen(
            mapContent = { TripMapArtwork() },
            onBackClick = {},
            onRecordClick = { destination = AppDestination.RECORDS },
            onCreateClick = { openCreateScreen(AppDestination.MAP) },
            onProfileClick = { destination = AppDestination.PROFILE },
        )

        AppDestination.RECORDS -> TripRecordListScreen(
            uiState = TripRecordListUiState.Success(
                records = visibleRecords,
                page = 0,
                totalPages = 1,
            ),
            query = query,
            locations = appLocations,
            onKeywordChanged = { keyword ->
                query = query.copy(keyword = keyword.ifBlank { null }, page = 0)
            },
            onLocationChanged = { locationId ->
                query = query.copy(locationId = locationId, page = 0)
            },
            onSearchClick = {},
            onPreviousPageClick = {},
            onNextPageClick = {},
            onCreateClick = { openCreateScreen(AppDestination.RECORDS) },
            onMapClick = { destination = AppDestination.MAP },
            onRecordClick = { recordId ->
                selectedRecordId = recordId
                destination = AppDestination.DETAIL
            },
            onProfileClick = { destination = AppDestination.PROFILE },
        )

        AppDestination.CREATE -> TripRecordEditorScreen(
            uiState = editorState,
            locations = appLocations,
            onProvinceChanged = {},
            onLocationSelected = { location ->
                editorState = editorState.copy(selectedLocation = location, errorMessage = null)
            },
            onTitleChanged = { title ->
                editorState = editorState.copy(title = title, errorMessage = null)
            },
            onContentChanged = { content ->
                editorState = editorState.copy(content = content, errorMessage = null)
            },
            onStartDateChanged = { date ->
                editorState = editorState.copy(startDate = date, errorMessage = null)
            },
            onEndDateChanged = { date ->
                editorState = editorState.copy(endDate = date, errorMessage = null)
            },
            onSaveClick = ::saveEditor,
            onBackClick = { destination = editorReturnDestination },
            onMapClick = { destination = AppDestination.MAP },
            onRecordClick = { destination = AppDestination.RECORDS },
            onProfileClick = { destination = AppDestination.PROFILE },
        )

        AppDestination.PROFILE -> TripProfileScreen(
            onMapClick = { destination = AppDestination.MAP },
            onRecordClick = { destination = AppDestination.RECORDS },
            onCreateClick = { openCreateScreen(AppDestination.PROFILE) },
            onProfileClick = { destination = AppDestination.PROFILE },
        )

        AppDestination.DETAIL -> TripRecordDetailScreen(
            uiState = selectedRecord?.let(TripRecordDetailUiState::Success)
                ?: TripRecordDetailUiState.Error("여행 기록을 찾을 수 없습니다."),
            locations = appLocations,
            onBackClick = { destination = AppDestination.RECORDS },
            onEditClick = { selectedRecord?.let(::openEditScreen) },
            onDeleteClick = {
                selectedRecordId?.let { recordId ->
                    records = records.filterNot { it.id == recordId }
                }
                destination = AppDestination.RECORDS
            },
            onMapClick = { destination = AppDestination.MAP },
            onRecordClick = { destination = AppDestination.RECORDS },
            onCreateClick = { openCreateScreen(AppDestination.DETAIL) },
            onProfileClick = { destination = AppDestination.PROFILE },
        )
    }
}

private val appLocations = listOf(
    Location(
        id = 1L,
        countryId = 1L,
        parentId = null,
        regionCode = "11",
        name = "서울특별시",
        type = LocationType.PROVINCE,
    ),
    Location(
        id = 2L,
        countryId = 1L,
        parentId = 1L,
        regionCode = "11680",
        name = "강남구",
        type = LocationType.DISTRICT,
    ),
    Location(
        id = 3L,
        countryId = 1L,
        parentId = 1L,
        regionCode = "11650",
        name = "서초구",
        type = LocationType.DISTRICT,
    ),
)
