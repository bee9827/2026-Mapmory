package com.mapmory.shared.presentation.travelrecord

import com.mapmory.shared.data.repository.FakeTravelRecordRepository
import com.mapmory.shared.domain.model.TravelRecordDraft
import com.mapmory.shared.domain.usecase.GetTravelRecordUseCase
import com.mapmory.shared.runSuspend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TravelRecordDetailViewModelTest {
    @Test
    fun loadChangesStateForSuccessAndFailure() {
        runSuspend {
            val repository = FakeTravelRecordRepository(10) { "2026-08-07T00:00:00Z" }
            val record = repository.createTravelRecord(
                TravelRecordDraft(
                    locationId = 101,
                    title = "서울 여행",
                    content = "한강을 걸었다.",
                    startDate = null,
                    endDate = null,
                    mediaObjectKeys = emptyList(),
                ),
            ).getOrThrow()
            val viewModel = TravelRecordDetailViewModel(GetTravelRecordUseCase(repository))

            viewModel.load(record.id)

            val success = assertIs<TravelRecordDetailUiState.Success>(viewModel.uiState)
            assertEquals("서울 여행", success.record.title)

            viewModel.load(Long.MAX_VALUE)

            assertIs<TravelRecordDetailUiState.Error>(viewModel.uiState)
        }
    }
}
