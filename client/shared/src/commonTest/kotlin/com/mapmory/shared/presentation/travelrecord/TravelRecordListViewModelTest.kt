package com.mapmory.shared.presentation.travelrecord

import com.mapmory.shared.runSuspend
import com.mapmory.shared.data.repository.FakeTravelRecordRepository
import com.mapmory.shared.domain.model.TravelRecordDraft
import com.mapmory.shared.domain.model.TravelRecordQuery
import com.mapmory.shared.domain.usecase.GetTravelRecordsUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TravelRecordListViewModelTest {
    @Test
    fun loadChangesStateForSuccessAndFailure() {
        runSuspend {
            val repository = FakeTravelRecordRepository(
                memberId = 10,
                now = { "2026-08-07T00:00:00Z" },
            )
            repository.createTravelRecord(
                TravelRecordDraft(
                    locationId = 101,
                    title = "서울 여행",
                    content = "한강을 걸었다.",
                    startDate = null,
                    endDate = null,
                    mediaObjectKeys = emptyList(),
                ),
            )
            repository.createTravelRecord(
                TravelRecordDraft(
                    locationId = 102,
                    title = "부산 여행",
                    content = "바다를 보았다.",
                    startDate = null,
                    endDate = null,
                    mediaObjectKeys = emptyList(),
                ),
            )
            val viewModel = TravelRecordListViewModel(GetTravelRecordsUseCase(repository))

            viewModel.load(TravelRecordQuery(size = 1))

            val success = assertIs<TravelRecordListUiState.Success>(viewModel.uiState)
            assertEquals("서울 여행", success.records.single().title)

            viewModel.nextPage()
            val nextPage = assertIs<TravelRecordListUiState.Success>(viewModel.uiState)
            assertEquals("부산 여행", nextPage.records.single().title)

            viewModel.previousPage()
            val previousPage = assertIs<TravelRecordListUiState.Success>(viewModel.uiState)
            assertEquals("서울 여행", previousPage.records.single().title)

            viewModel.updateKeyword("없는 기록")
            viewModel.load()

            val empty = assertIs<TravelRecordListUiState.Success>(viewModel.uiState)
            assertEquals(0, empty.records.size)

            viewModel.load(TravelRecordQuery(size = 0))

            assertIs<TravelRecordListUiState.Error>(viewModel.uiState)
        }
    }
}
