package com.mapmory.shared.presentation.triprecord.viewmodel

import com.mapmory.shared.data.repository.FakeTripRecordRepository
import com.mapmory.shared.domain.model.TripRecordDraft
import com.mapmory.shared.domain.model.TripRecordQuery
import com.mapmory.shared.domain.usecase.GetTripRecordsUseCase
import com.mapmory.shared.presentation.triprecord.state.TripRecordListUiState
import com.mapmory.shared.runSuspend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TripRecordListViewModelTest {
    @Test
    fun `새로고침은_기록_변경_후_현재_필터를_다시_조회한다`() = runSuspend {
        val repository = FakeTripRecordRepository { "2026-08-07T00:00:00Z" }
        val original = repository.createTripRecord(
            TripRecordDraft(
                locationId = 101,
                title = "수정 전",
                content = "",
                startDate = "2026-08-01",
                endDate = null,
                mediaObjectKeys = emptyList(),
            ),
        ).getOrThrow()
        val viewModel = TripRecordListViewModel(GetTripRecordsUseCase(repository))
        viewModel.initialize(locationId = 101)

        repository.updateTripRecord(
            original.id,
            TripRecordDraft(
                locationId = 101,
                title = "수정 후",
                content = "",
                startDate = "2026-08-01",
                endDate = null,
                mediaObjectKeys = emptyList(),
            ),
        ).getOrThrow()
        viewModel.refresh(locationId = 101)

        val state = assertIs<TripRecordListUiState.Success>(viewModel.uiState)
        assertEquals("수정 후", state.records.single().title)
        assertEquals(101, viewModel.query.locationId)
    }

    @Test
    fun `경로를_반복_초기화해도_현재_필터를_유지한다`() = runSuspend {
        val repository = FakeTripRecordRepository { "2026-08-07T00:00:00Z" }
        val viewModel = TripRecordListViewModel(GetTripRecordsUseCase(repository))

        viewModel.initialize(locationId = 101)
        viewModel.initialize(locationId = 101)

        assertEquals(101, viewModel.query.locationId)
    }

    @Test
    fun `로드는_성공과_실패에_따라_상태를_변경한다`() {
        runSuspend {
            val repository = FakeTripRecordRepository(
                now = { "2026-08-07T00:00:00Z" },
            )
            repository.createTripRecord(
                TripRecordDraft(
                    locationId = 101,
                    title = "서울 여행",
                    content = "한강을 걸었다.",
                    startDate = "2026-08-01",
                    endDate = null,
                    mediaObjectKeys = emptyList(),
                ),
            )
            repository.createTripRecord(
                TripRecordDraft(
                    locationId = 102,
                    title = "부산 여행",
                    content = "바다를 보았다.",
                    startDate = "2026-08-02",
                    endDate = null,
                    mediaObjectKeys = emptyList(),
                ),
            )
            val viewModel = TripRecordListViewModel(GetTripRecordsUseCase(repository))

            viewModel.load(TripRecordQuery(size = 1))

            val success = assertIs<TripRecordListUiState.Success>(viewModel.uiState)
            assertEquals("서울 여행", success.records.single().title)

            viewModel.nextPage()
            val nextPage = assertIs<TripRecordListUiState.Success>(viewModel.uiState)
            assertEquals("부산 여행", nextPage.records.single().title)

            viewModel.previousPage()
            val previousPage = assertIs<TripRecordListUiState.Success>(viewModel.uiState)
            assertEquals("서울 여행", previousPage.records.single().title)

            viewModel.load(TripRecordQuery(size = 0))

            assertIs<TripRecordListUiState.Error>(viewModel.uiState)
        }
    }
}
