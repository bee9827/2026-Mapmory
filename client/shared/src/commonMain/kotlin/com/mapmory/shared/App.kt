package com.mapmory.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.mapmory.shared.data.repository.FakeTravelRecordRepository
import com.mapmory.shared.domain.usecase.GetTravelRecordsUseCase
import com.mapmory.shared.presentation.travelrecord.TravelRecordListScreen
import com.mapmory.shared.presentation.travelrecord.TravelRecordListViewModel

@Composable
fun MapmoryApp() {
    val viewModel = remember {
        TravelRecordListViewModel(
            GetTravelRecordsUseCase(
                FakeTravelRecordRepository(
                    memberId = 10,
                    now = { "2026-08-07T00:00:00Z" },
                ),
            ),
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.load()
    }

    TravelRecordListScreen(uiState = viewModel.uiState)
}
