package com.mapmory.shared.presentation.triprecord.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.mapmory.shared.domain.region.RegionCatalog
import com.mapmory.shared.presentation.triprecord.screen.TripRecordListScreen
import com.mapmory.shared.presentation.triprecord.state.TripRecordFilterUiState
import com.mapmory.shared.presentation.triprecord.viewmodel.TripRecordListViewModel
import kotlinx.coroutines.launch

@Composable
internal fun TripRecordListRoute(
    viewModel: TripRecordListViewModel,
    regionCatalog: RegionCatalog,
    initialLocationId: Long?,
    onOpenMap: () -> Unit,
    onOpenEditor: () -> Unit,
    onOpenDetail: (Long) -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel, initialLocationId) {
        viewModel.initialize(initialLocationId)
    }

    TripRecordListScreen(
        modifier = modifier,
        uiState = viewModel.uiState,
        filter = TripRecordFilterUiState(
            locationId = viewModel.query.locationId,
            keyword = viewModel.query.keyword.orEmpty(),
        ),
        locations = regionCatalog.locations,
        onKeywordChanged = { keyword ->
            viewModel.updateKeyword(keyword)
        },
        onLocationChanged = { locationId ->
            viewModel.filterByLocation(locationId)
            scope.launch { viewModel.load() }
        },
        onSearchClick = {
            scope.launch { viewModel.load() }
        },
        onPreviousPageClick = {
            scope.launch { viewModel.previousPage() }
        },
        onNextPageClick = {
            scope.launch { viewModel.nextPage() }
        },
        onCreateClick = onOpenEditor,
        onMapClick = onOpenMap,
        onRecordClick = onOpenDetail,
        onProfileClick = onOpenProfile,
    )
}
