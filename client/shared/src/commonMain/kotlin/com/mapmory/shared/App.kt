package com.mapmory.shared

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mapmory.shared.domain.model.KoreanCountryNames
import com.mapmory.shared.domain.model.KoreanDistrictCodes
import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.LocationType
import com.mapmory.shared.presentation.map.data.GeneratedWorldMapData
import com.mapmory.shared.presentation.map.domain.MapScope
import com.mapmory.shared.presentation.map.ui.MapArtwork
import com.mapmory.shared.presentation.triprecord.screen.TripMapScreen
import com.mapmory.shared.presentation.triprecord.screen.TripProfileScreen
import com.mapmory.shared.presentation.triprecord.screen.TripRecordDetailScreen
import com.mapmory.shared.presentation.triprecord.screen.TripRecordEditorScreen
import com.mapmory.shared.presentation.triprecord.screen.TripRecordListScreen
import com.mapmory.shared.presentation.triprecord.state.TripRecordDetailUiState
import com.mapmory.shared.presentation.triprecord.state.TripRecordEffect
import com.mapmory.shared.presentation.triprecord.state.TripRecordListUiState
import com.mapmory.shared.presentation.triprecord.viewmodel.TripRecordAction
import com.mapmory.shared.presentation.triprecord.viewmodel.TripRecordsViewModel
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
fun MapmoryApp(
    navigation: MapmoryNavigation? = null,
    contentWindowInsets: WindowInsets = WindowInsets(0, 0, 0, 0),
) {
    val navController = rememberNavController()
    val recordsViewModel = remember { TripRecordsViewModel(appLocations) }
    val recordsUiState = recordsViewModel.uiState

    fun navigateBack(): Boolean {
        if (navController.currentDestination?.id == navController.graph.startDestinationId) {
            return false
        }
        if (navController.popBackStack()) {
            return true
        }

        // popBackStack can empty the stack when a destination has no parent.
        // Restore the home route instead of leaving NavHost without content.
        navController.navigate(MapRoute) {
            launchSingleTop = true
        }
        return true
    }

    DisposableEffect(navigation, navController) {
        navigation?.bindBackHandler(::navigateBack)
        onDispose { navigation?.unbindBackHandler() }
    }

    var mapScope by remember { mutableStateOf(MapScope.WORLD) }
    val locationsById = remember { appLocations.associateBy(Location::id) }

    fun navigateToTab(route: Any) {
        navController.navigate(route) {
            popUpTo<MapRoute> {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    fun handleMapLocationClick(regionCode: String) {
        val location = appLocations.firstOrNull { it.regionCode == regionCode } ?: return
        recordsViewModel.onAction(TripRecordAction.MapLocationSelected(location))
    }

    LaunchedEffect(recordsUiState.effect) {
        val effect = recordsUiState.effect ?: return@LaunchedEffect
        when (effect) {
            TripRecordEffect.OpenRecords -> navigateToTab(RecordsRoute)
            TripRecordEffect.OpenEditor -> navController.navigate(CreateRoute)
            is TripRecordEffect.OpenDetail -> {
                val replaced = effect.replaceCurrent && navController.popBackStack()
                if (!replaced) navController.navigate(DetailRoute(effect.recordId))
            }
            TripRecordEffect.CloseDetail -> {
                if (!navController.popBackStack()) navigateToTab(RecordsRoute)
            }
        }
        recordsViewModel.onAction(TripRecordAction.EffectHandled)
    }

    val visitedLocations = recordsUiState.records.mapNotNull { record ->
        appLocations.firstOrNull { it.name == record.locationName }
    }
    val visitedCountryCodes = visitedLocations.map { location ->
        if (location.countryId == 1L) "KR" else location.regionCode
    }.toSet()
    val visitedRegionCodes = visitedLocations.mapNotNull { location ->
        when {
            location.countryId != 1L -> null
            location.type == LocationType.PROVINCE -> location.regionCode
            else -> locationsById[location.parentId]?.regionCode
        }
    }.toSet()

    NavHost(
        navController = navController,
        startDestination = MapRoute,
    ) {
        composable<MapRoute> {
            TripMapScreen(
                modifier = Modifier.windowInsetsPadding(contentWindowInsets),
                mapScope = mapScope,
                visitedCount = if (mapScope == MapScope.WORLD) {
                    visitedCountryCodes.size
                } else {
                    visitedRegionCodes.size
                },
                onMapScopeChange = { mapScope = it },
                mapContent = {
                    // Map taps are resolved to a location and routed to records or the editor.
                    MapArtwork(
                        scope = mapScope,
                        visitedCountryCodes = visitedCountryCodes,
                        visitedRegionCodes = visitedRegionCodes,
                        onCountryClick = { countryCode ->
                            handleMapLocationClick(countryCode)
                        },
                        onRegionClick = { regionCode ->
                            handleMapLocationClick(regionCode)
                        },
                    )
                },
                onBackClick = {},
                onRecordClick = { navigateToTab(RecordsRoute) },
                onCreateClick = {
                    recordsViewModel.onAction(TripRecordAction.StartCreating())
                },
                onProfileClick = { navigateToTab(ProfileRoute) },
            )
        }

        composable<RecordsRoute> {
            TripRecordListScreen(
                modifier = Modifier.windowInsetsPadding(contentWindowInsets),
                uiState = TripRecordListUiState.Success(
                    records = recordsUiState.visibleRecords,
                    page = 0,
                    totalPages = 1,
                ),
                filter = recordsUiState.filter,
                locations = appLocations,
                onKeywordChanged = { keyword ->
                    recordsViewModel.onAction(TripRecordAction.KeywordChanged(keyword))
                },
                onLocationChanged = { locationId ->
                    recordsViewModel.onAction(TripRecordAction.LocationFilterChanged(locationId))
                },
                onSearchClick = {},
                onPreviousPageClick = {},
                onNextPageClick = {},
                onCreateClick = {
                    recordsViewModel.onAction(TripRecordAction.StartCreating())
                },
                onMapClick = { navigateToTab(MapRoute) },
                onRecordClick = { recordId ->
                    recordsViewModel.onAction(TripRecordAction.RecordSelected(recordId))
                },
                onProfileClick = { navigateToTab(ProfileRoute) },
            )
        }

        composable<CreateRoute> {
            TripRecordEditorScreen(
                modifier = Modifier.windowInsetsPadding(contentWindowInsets),
                uiState = recordsUiState.editor,
                locations = appLocations,
                onLocationSelected = { location ->
                    recordsViewModel.onAction(TripRecordAction.LocationSelected(location))
                },
                onTitleChanged = { title ->
                    recordsViewModel.onAction(TripRecordAction.TitleChanged(title))
                },
                onContentChanged = { content ->
                    recordsViewModel.onAction(TripRecordAction.ContentChanged(content))
                },
                onStartDateChanged = { date ->
                    recordsViewModel.onAction(TripRecordAction.StartDateChanged(date))
                },
                onEndDateChanged = { date ->
                    recordsViewModel.onAction(TripRecordAction.EndDateChanged(date))
                },
                onPhotosAdded = { photos ->
                    recordsViewModel.onAction(TripRecordAction.PhotosAdded(photos))
                },
                onPhotoRemoved = { photoId ->
                    recordsViewModel.onAction(TripRecordAction.PhotoRemoved(photoId))
                },
                onSaveClick = { recordsViewModel.onAction(TripRecordAction.Save) },
                onBackClick = { navigateBack() },
                onMapClick = { navigateToTab(MapRoute) },
                onRecordClick = { navigateToTab(RecordsRoute) },
                onProfileClick = { navigateToTab(ProfileRoute) },
            )
        }

        composable<ProfileRoute> {
            TripProfileScreen(
                modifier = Modifier.windowInsetsPadding(contentWindowInsets),
                onMapClick = { navigateToTab(MapRoute) },
                onRecordClick = { navigateToTab(RecordsRoute) },
                onCreateClick = {
                    recordsViewModel.onAction(TripRecordAction.StartCreating())
                },
                onProfileClick = { navigateToTab(ProfileRoute) },
            )
        }

        composable<DetailRoute> { backStackEntry ->
            val detailRoute = backStackEntry.toRoute<DetailRoute>()
            val selectedRecord = recordsUiState.records.firstOrNull { it.id == detailRoute.recordId }
            TripRecordDetailScreen(
                modifier = Modifier.windowInsetsPadding(
                    contentWindowInsets.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                    ),
                ),
                uiState = selectedRecord?.let(TripRecordDetailUiState::Success)
                    ?: TripRecordDetailUiState.Error("여행 기록을 찾을 수 없습니다."),
                onBackClick = { navigateBack() },
                onEditClick = {
                    recordsViewModel.onAction(TripRecordAction.StartEditing(detailRoute.recordId))
                },
                onDeleteClick = {
                    recordsViewModel.onAction(TripRecordAction.Delete(detailRoute.recordId))
                },
                onMapClick = { navigateToTab(MapRoute) },
                onRecordClick = { navigateToTab(RecordsRoute) },
                onCreateClick = {
                    recordsViewModel.onAction(TripRecordAction.StartCreating())
                },
                onProfileClick = { navigateToTab(ProfileRoute) },
            )
        }
    }
}

private val appLocations = buildList {
    add(
        Location(
            id = 1L,
            countryId = 1L,
            parentId = null,
            regionCode = "KR-11",
            name = "서울특별시",
            type = LocationType.PROVINCE,
        ),
    )
    add(
        Location(
            id = 2L,
            countryId = 1L,
            parentId = 1L,
            regionCode = "11680",
            name = "강남구",
            type = LocationType.DISTRICT,
        ),
    )
    add(
        Location(
            id = 3L,
            countryId = 1L,
            parentId = 1L,
            regionCode = "11650",
            name = "서초구",
            type = LocationType.DISTRICT,
        ),
    )

    listOf(
        4L to ("KR-26" to "부산광역시"),
        5L to ("KR-27" to "대구광역시"),
        6L to ("KR-28" to "인천광역시"),
        7L to ("KR-29" to "광주광역시"),
        8L to ("KR-30" to "대전광역시"),
        9L to ("KR-31" to "울산광역시"),
        10L to ("KR-50" to "세종특별자치시"),
        11L to ("KR-41" to "경기도"),
        12L to ("KR-42" to "강원특별자치도"),
        13L to ("KR-43" to "충청북도"),
        14L to ("KR-44" to "충청남도"),
        15L to ("KR-45" to "전북특별자치도"),
        16L to ("KR-46" to "전라남도"),
        17L to ("KR-47" to "경상북도"),
        18L to ("KR-48" to "경상남도"),
        19L to ("KR-49" to "제주특별자치도"),
    ).forEach { (id, region) ->
        add(
            Location(
                id = id,
                countryId = 1L,
                parentId = null,
                regionCode = region.first,
                name = region.second,
                type = LocationType.PROVINCE,
            ),
        )
    }

    val koreaProvinceIds = filter {
        it.countryId == 1L && it.type == LocationType.PROVINCE
    }.associate { it.regionCode to it.id }

    KoreanDistrictCodes.forEachIndexed { index, district ->
        val id = when (district.code) {
            "11650" -> 3L
            "11680" -> 2L
            else -> 20_000L + index
        }
        add(
            Location(
                id = id,
                countryId = 1L,
                parentId = district.provinceCode?.let { koreaProvinceIds[it] },
                regionCode = district.code,
                name = district.name,
                type = LocationType.DISTRICT,
            ),
        )
    }

    GeneratedWorldMapData.countries.forEachIndexed { index, country ->
        val id = 10_000L + index
        add(
            Location(
                id = id,
                countryId = id,
                parentId = null,
                regionCode = country.code,
                name = KoreanCountryNames.byCode[country.code] ?: country.name,
                type = LocationType.PROVINCE,
            ),
        )
    }
}
