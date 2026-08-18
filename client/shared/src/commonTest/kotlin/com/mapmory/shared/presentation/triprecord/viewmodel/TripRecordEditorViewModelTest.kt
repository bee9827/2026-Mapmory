package com.mapmory.shared.presentation.triprecord.viewmodel

import com.mapmory.shared.data.repository.FakeTripRecordRepository
import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.LocationType
import com.mapmory.shared.domain.model.TripRecordQuery
import com.mapmory.shared.domain.usecase.CreateTripRecordUseCase
import com.mapmory.shared.domain.usecase.GetTripRecordsUseCase
import com.mapmory.shared.domain.usecase.UpdateTripRecordUseCase
import com.mapmory.shared.presentation.triprecord.state.TripRecordEditorErrorTarget
import com.mapmory.shared.runSuspend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TripRecordEditorViewModelTest {
    @Test
    fun saveCreatesAndUpdatesTripRecord() {
        runSuspend {
            val repository = FakeTripRecordRepository(10) { "2026-08-07T00:00:00Z" }
            val viewModel = TripRecordEditorViewModel(
                createTripRecord = CreateTripRecordUseCase(repository),
                updateTripRecord = UpdateTripRecordUseCase(repository),
            )

            viewModel.selectLocation(Location(101, 1, 1, "11680", "강남구", LocationType.DISTRICT))
            viewModel.updateTitle("서울 여행")
            viewModel.updateContent("한강을 걸었다.")

            assertTrue(viewModel.save())
            assertEquals(
                "서울 여행",
                GetTripRecordsUseCase(repository)(TripRecordQuery()).getOrThrow().records.single().title,
            )

            val record = repository.getTripRecords(TripRecordQuery()).getOrThrow().records.single()
            viewModel.startEditing(
                record = record,
                location = Location(101, 1, 1, "11680", "강남구", LocationType.DISTRICT),
            )
            viewModel.clearLocation()
            assertNull(viewModel.uiState.selectedLocation)
            viewModel.selectLocation(Location(101, 1, 1, "11680", "강남구", LocationType.DISTRICT))
            viewModel.updateTitle("서울 여름 여행")

            assertTrue(viewModel.save())
            assertEquals("서울 여름 여행", repository.getTripRecord(record.id).getOrThrow().title)
        }
    }

    @Test
    fun saveAllowsEitherDateToBeOmittedAndRejectsInvalidDateRange() {
        runSuspend {
            val repository = FakeTripRecordRepository(10) { "2026-08-07T00:00:00Z" }
            val viewModel = TripRecordEditorViewModel(
                createTripRecord = CreateTripRecordUseCase(repository),
                updateTripRecord = UpdateTripRecordUseCase(repository),
            )

            viewModel.selectLocation(Location(101, 1, 1, "11680", "강남구", LocationType.DISTRICT))
            viewModel.updateTitle("서울 여행")
            viewModel.updateEndDate("2026-08-01")

            assertTrue(viewModel.save())
            assertTrue(viewModel.uiState.fieldErrors.isEmpty())

            viewModel.updateStartDate("2026-08-02")
            assertEquals("종료일은 시작일보다 빠를 수 없습니다.", viewModel.uiState.errorMessage)
            assertEquals(TripRecordEditorErrorTarget.START_DATE, viewModel.uiState.errorTarget)
            assertFalse(viewModel.save())
            assertEquals("종료일은 시작일보다 빠를 수 없습니다.", viewModel.uiState.errorMessage)
            assertEquals(TripRecordEditorErrorTarget.END_DATE, viewModel.uiState.errorTarget)

            viewModel.updateStartDate("2026-02-29")
            viewModel.updateEndDate("")
            assertEquals("올바른 시작일을 입력해 주세요.", viewModel.uiState.errorMessage)
            assertEquals(TripRecordEditorErrorTarget.START_DATE, viewModel.uiState.errorTarget)
            assertFalse(viewModel.save())
            assertEquals("올바른 시작일을 입력해 주세요.", viewModel.uiState.errorMessage)
            assertEquals(TripRecordEditorErrorTarget.START_DATE, viewModel.uiState.errorTarget)
        }
    }

    @Test
    fun editingShowsOnlyTheTouchedFieldErrorImmediately() {
        runSuspend {
            val repository = FakeTripRecordRepository(10) { "2026-08-07T00:00:00Z" }
            val viewModel = TripRecordEditorViewModel(
                createTripRecord = CreateTripRecordUseCase(repository),
                updateTripRecord = UpdateTripRecordUseCase(repository),
            )
            assertFalse(viewModel.uiState.isDirty)
            viewModel.updateContent("작성 시작")

            assertTrue(viewModel.uiState.isDirty)
            assertTrue(viewModel.uiState.fieldErrors.isEmpty())

            viewModel.updateTitle(" ")
            assertEquals(
                mapOf(TripRecordEditorErrorTarget.TITLE to "제목을 입력해 주세요."),
                viewModel.uiState.fieldErrors,
            )

            viewModel.touchLocation()
            assertEquals(
                mapOf(
                    TripRecordEditorErrorTarget.LOCATION to "장소를 선택해 주세요.",
                    TripRecordEditorErrorTarget.TITLE to "제목을 입력해 주세요.",
                ),
                viewModel.uiState.fieldErrors,
            )

            viewModel.selectLocation(Location(101, 1, 1, "11680", "강남구", LocationType.DISTRICT))
            assertEquals(
                mapOf(TripRecordEditorErrorTarget.TITLE to "제목을 입력해 주세요."),
                viewModel.uiState.fieldErrors,
            )
            viewModel.updateTitle("서울 여행")
            assertTrue(viewModel.uiState.fieldErrors.isEmpty())
        }
    }

    @Test
    fun mediaObjectKeysCanBeAddedAndRemoved() {
        val viewModel = TripRecordEditorViewModel(
            createTripRecord = CreateTripRecordUseCase(FakeTripRecordRepository(10) { "2026-08-07T00:00:00Z" }),
            updateTripRecord = UpdateTripRecordUseCase(FakeTripRecordRepository(10) { "2026-08-07T00:00:00Z" }),
        )

        viewModel.addMediaObjectKey(" records/1/photo.jpg ")
        viewModel.addMediaObjectKey("records/1/photo.jpg")
        viewModel.addMediaObjectKey(" ")

        assertEquals(listOf("records/1/photo.jpg"), viewModel.uiState.mediaObjectKeys)

        viewModel.removeMediaObjectKey("records/1/photo.jpg")

        assertTrue(viewModel.uiState.mediaObjectKeys.isEmpty())
    }
}
