package com.mapmory.shared.presentation.travelrecord

import com.mapmory.shared.data.repository.FakeTravelRecordRepository
import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.LocationType
import com.mapmory.shared.domain.model.TravelRecordQuery
import com.mapmory.shared.domain.usecase.CreateTravelRecordUseCase
import com.mapmory.shared.domain.usecase.GetTravelRecordsUseCase
import com.mapmory.shared.domain.usecase.UpdateTravelRecordUseCase
import com.mapmory.shared.runSuspend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TravelRecordEditorViewModelTest {
    @Test
    fun saveCreatesAndUpdatesTravelRecord() {
        runSuspend {
            val repository = FakeTravelRecordRepository(10) { "2026-08-07T00:00:00Z" }
            val viewModel = TravelRecordEditorViewModel(
                createTravelRecord = CreateTravelRecordUseCase(repository),
                updateTravelRecord = UpdateTravelRecordUseCase(repository),
            )

            viewModel.selectLocation(Location(101, 1, 1, "KR-11-11680", "강남구", LocationType.DISTRICT))
            viewModel.updateTitle("서울 여행")
            viewModel.updateContent("한강을 걸었다.")

            assertTrue(viewModel.save())
            assertEquals(
                "서울 여행",
                GetTravelRecordsUseCase(repository)(TravelRecordQuery()).getOrThrow().records.single().title,
            )

            val record = repository.getTravelRecords(TravelRecordQuery()).getOrThrow().records.single()
            viewModel.startEditing(
                record = record,
                location = Location(101, 1, 1, "KR-11-11680", "강남구", LocationType.DISTRICT),
            )
            viewModel.clearLocation()
            assertNull(viewModel.uiState.selectedLocation)
            viewModel.selectLocation(Location(101, 1, 1, "KR-11-11680", "강남구", LocationType.DISTRICT))
            viewModel.updateTitle("서울 여름 여행")

            assertTrue(viewModel.save())
            assertEquals("서울 여름 여행", repository.getTravelRecord(record.id).getOrThrow().title)
        }
    }
}
