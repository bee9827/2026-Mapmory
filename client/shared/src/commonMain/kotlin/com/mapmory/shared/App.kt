package com.mapmory.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mapmory.shared.data.repository.FakeTravelRecordRepository
import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.LocationType
import com.mapmory.shared.domain.usecase.CreateTravelRecordUseCase
import com.mapmory.shared.domain.usecase.DeleteTravelRecordUseCase
import com.mapmory.shared.domain.usecase.GetTravelRecordUseCase
import com.mapmory.shared.domain.usecase.GetTravelRecordsUseCase
import com.mapmory.shared.domain.usecase.UpdateTravelRecordUseCase
import com.mapmory.shared.presentation.travelrecord.TravelRecordDetailScreen
import com.mapmory.shared.presentation.travelrecord.TravelRecordDetailUiState
import com.mapmory.shared.presentation.travelrecord.TravelRecordDetailViewModel
import com.mapmory.shared.presentation.travelrecord.TravelRecordEditorScreen
import com.mapmory.shared.presentation.travelrecord.TravelRecordEditorViewModel
import com.mapmory.shared.presentation.travelrecord.TravelRecordListScreen
import com.mapmory.shared.presentation.travelrecord.TravelRecordListViewModel
import kotlinx.coroutines.launch

@Composable
fun MapmoryApp() {
    val repository = remember {
        FakeTravelRecordRepository(
            memberId = 10,
            now = { "2026-08-07T00:00:00Z" },
        )
    }
    val listViewModel = remember { TravelRecordListViewModel(GetTravelRecordsUseCase(repository)) }
    val detailViewModel = remember {
        TravelRecordDetailViewModel(
            getTravelRecord = GetTravelRecordUseCase(repository),
            deleteTravelRecord = DeleteTravelRecordUseCase(repository),
        )
    }
    val editorViewModel = remember {
        TravelRecordEditorViewModel(
            createTravelRecord = CreateTravelRecordUseCase(repository),
            updateTravelRecord = UpdateTravelRecordUseCase(repository),
        )
    }
    val scope = rememberCoroutineScope()
    var screen by remember { mutableStateOf(AppScreen.LIST) }
    var selectedRecordId by remember { mutableStateOf<Long?>(null) }

    // ponytail: 임시 지역 목록이며, 장소 조회 API를 연결하면 Repository 결과로 교체한다.
    val locations = remember {
        listOf(
            Location(101, 1, 1, "KR-11-11680", "강남구", LocationType.DISTRICT),
            Location(102, 1, 1, "KR-26-26290", "해운대구", LocationType.DISTRICT),
        )
    }

    LaunchedEffect(screen) {
        if (screen == AppScreen.LIST) listViewModel.load()
        if (screen == AppScreen.DETAIL) selectedRecordId?.let { detailViewModel.load(it) }
    }

    when (screen) {
        AppScreen.LIST -> TravelRecordListScreen(
            uiState = listViewModel.uiState,
            onCreateClick = {
                editorViewModel.reset()
                screen = AppScreen.EDITOR
            },
            onRecordClick = { recordId ->
                selectedRecordId = recordId
                screen = AppScreen.DETAIL
            },
        )

        AppScreen.EDITOR -> TravelRecordEditorScreen(
            uiState = editorViewModel.uiState,
            locations = locations,
            onLocationSelected = editorViewModel::selectLocation,
            onTitleChanged = editorViewModel::updateTitle,
            onContentChanged = editorViewModel::updateContent,
            onStartDateChanged = editorViewModel::updateStartDate,
            onEndDateChanged = editorViewModel::updateEndDate,
            onSaveClick = {
                scope.launch {
                    if (editorViewModel.save()) screen = AppScreen.LIST
                }
            },
            onBackClick = { screen = AppScreen.LIST },
        )

        AppScreen.DETAIL -> TravelRecordDetailScreen(
            uiState = detailViewModel.uiState,
            onBackClick = { screen = AppScreen.LIST },
            onEditClick = {
                val record = (detailViewModel.uiState as? TravelRecordDetailUiState.Success)?.record
                val location = locations.firstOrNull { it.id == record?.locationId }
                if (record != null && location != null) {
                    editorViewModel.startEditing(record, location)
                    screen = AppScreen.EDITOR
                }
            },
            onDeleteClick = {
                scope.launch {
                    if (detailViewModel.delete()) {
                        selectedRecordId = null
                        screen = AppScreen.LIST
                    }
                }
            },
        )
    }
}

private enum class AppScreen {
    LIST,
    EDITOR,
    DETAIL,
}
