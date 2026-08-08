package com.mapmory.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.LocationType
import com.mapmory.shared.domain.model.TripRecordData
import com.mapmory.shared.domain.model.TripRecordQuery
import com.mapmory.shared.presentation.map.domain.MapScope
import com.mapmory.shared.presentation.map.ui.MapArtwork
import com.mapmory.shared.presentation.triprecord.screen.TripMapScreen
import com.mapmory.shared.presentation.triprecord.screen.TripProfileScreen
import com.mapmory.shared.presentation.triprecord.screen.TripRecordDetailScreen
import com.mapmory.shared.presentation.triprecord.screen.TripRecordEditorScreen
import com.mapmory.shared.presentation.triprecord.screen.TripRecordListScreen
import com.mapmory.shared.presentation.triprecord.state.TripRecordDetailUiState
import com.mapmory.shared.presentation.triprecord.state.TripRecordEditorUiState
import com.mapmory.shared.presentation.triprecord.state.TripRecordListUiState
import kotlinx.serialization.Serializable

@Serializable
private data object MapRoute

@Serializable
private data object RecordsRoute

@Serializable
private data object CreateRoute

@Serializable
private data object ProfileRoute

@Serializable
private data class DetailRoute(
    val recordId: Long,
)

@Composable
fun MapmoryApp() {
    val navController = rememberNavController()
    var mapScope by remember { mutableStateOf(MapScope.WORLD) }
    var records by remember { mutableStateOf(emptyList<TripRecordData>()) }
    var query by remember { mutableStateOf(TripRecordQuery()) }
    var editorState by remember {
        mutableStateOf(
            TripRecordEditorUiState(
                selectedLocation = appLocations.firstOrNull { it.type == LocationType.DISTRICT },
            ),
        )
    }

    fun navigateToTab(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun openCreateScreen() {
        editorState = TripRecordEditorUiState(
            selectedLocation = appLocations.firstOrNull { it.type == LocationType.DISTRICT },
        )
        navController.navigate(CreateRoute)
    }

    fun openEditScreen(record: TripRecordData) {
        val location = appLocations.firstOrNull { it.id == record.locationId }
            ?: appLocations.firstOrNull { it.type == LocationType.DISTRICT }
        editorState = TripRecordEditorUiState(
            recordId = record.id,
            selectedLocation = location,
            title = record.title,
            content = record.content,
            startDate = record.startDate.orEmpty(),
            endDate = record.endDate.orEmpty(),
        )
        navController.navigate(CreateRoute)
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
                query = TripRecordQuery()
                if (!navController.popBackStack()) {
                    navigateToTab(RecordsRoute)
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

    NavHost(
        navController = navController,
        startDestination = MapRoute,
    ) {
        composable<MapRoute> {
            TripMapScreen(
                mapScope = mapScope,
                onMapScopeChange = { mapScope = it },
                mapContent = {
                    // Temporary sample state until saved records are mapped to country codes.
                    MapArtwork(
                        scope = mapScope,
                        visitedCountryCodes = setOf("KOR"),
                        visitedRegionCodes = setOf("KR-46"),
                    )
                },
                onBackClick = {},
                onRecordClick = { navigateToTab(RecordsRoute) },
                onCreateClick = { openCreateScreen() },
                onProfileClick = { navigateToTab(ProfileRoute) },
            )
        }

        composable<RecordsRoute> {
            TripRecordListScreen(
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
                onCreateClick = { openCreateScreen() },
                onMapClick = { navigateToTab(MapRoute) },
                onRecordClick = { recordId ->
                    navController.navigate(DetailRoute(recordId))
                },
                onProfileClick = { navigateToTab(ProfileRoute) },
            )
        }

        composable<CreateRoute> {
            TripRecordEditorScreen(
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
                onBackClick = { navController.popBackStack() },
                onMapClick = { navigateToTab(MapRoute) },
                onRecordClick = { navigateToTab(RecordsRoute) },
                onProfileClick = { navigateToTab(ProfileRoute) },
            )
        }

        composable<ProfileRoute> {
            TripProfileScreen(
                onMapClick = { navigateToTab(MapRoute) },
                onRecordClick = { navigateToTab(RecordsRoute) },
                onCreateClick = { openCreateScreen() },
                onProfileClick = { navigateToTab(ProfileRoute) },
            )
        }

        composable<DetailRoute> { backStackEntry ->
            val detailRoute = backStackEntry.toRoute<DetailRoute>()
            val selectedRecord = records.firstOrNull { it.id == detailRoute.recordId }
            TripRecordDetailScreen(
                uiState = selectedRecord?.let(TripRecordDetailUiState::Success)
                    ?: TripRecordDetailUiState.Error("여행 기록을 찾을 수 없습니다."),
                locations = appLocations,
                onBackClick = { navController.popBackStack() },
                onEditClick = { selectedRecord?.let(::openEditScreen) },
                onDeleteClick = {
                    records = records.filterNot { it.id == detailRoute.recordId }
                    navController.popBackStack()
                },
                onMapClick = { navigateToTab(MapRoute) },
                onRecordClick = { navigateToTab(RecordsRoute) },
                onCreateClick = { openCreateScreen() },
                onProfileClick = { navigateToTab(ProfileRoute) },
            )
        }
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
