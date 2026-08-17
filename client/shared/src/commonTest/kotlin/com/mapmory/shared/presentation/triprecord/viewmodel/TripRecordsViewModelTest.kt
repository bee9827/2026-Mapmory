package com.mapmory.shared.presentation.triprecord.viewmodel

import com.mapmory.shared.domain.TripRecord
import com.mapmory.shared.domain.TripRecords
import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.LocationType
import com.mapmory.shared.presentation.photo.SelectedPhoto
import com.mapmory.shared.presentation.triprecord.state.TripRecordEffect
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TripRecordsViewModelTest {
    @Test
    fun `화면 액션으로 도메인 여행 기록을 생성하고 UI 상태를 발행한다`() {
        val viewModel = TripRecordsViewModel(locations)
        val initialState = viewModel.uiState

        viewModel.onAction(TripRecordAction.StartCreating(gangnam))
        viewModel.onAction(TripRecordAction.EffectHandled)
        viewModel.onAction(TripRecordAction.TitleChanged("서울 여행"))
        viewModel.onAction(TripRecordAction.ContentChanged("한강을 걸었다."))
        viewModel.onAction(TripRecordAction.StartDateChanged("2026.08.01"))
        viewModel.onAction(TripRecordAction.EndDateChanged("2026.08.03"))
        viewModel.onAction(
            TripRecordAction.PhotosAdded(
                listOf(
                    SelectedPhoto(
                        id = "photo-1",
                        displayName = "서울.jpg",
                        previewBytes = byteArrayOf(1, 2, 3),
                    ),
                ),
            ),
        )
        viewModel.onAction(TripRecordAction.Save)

        assertTrue(initialState.records.isEmpty())
        val record = viewModel.uiState.records.single()
        assertEquals("서울 여행", record.title)
        assertEquals("한강을 걸었다.", record.content)
        assertEquals("강남구", record.locationName)
        assertEquals("2026-08-01", record.startDate)
        assertEquals("2026-08-03", record.endDate)
        assertContentEquals(
            byteArrayOf(1, 2, 3),
            record.photos.single().previewBytes?.bytesForDecoding(),
        )
        assertEquals(TripRecordEffect.OpenRecords, viewModel.uiState.effect)
    }

    @Test
    fun `기록 수정과 삭제는 TripRecords 결과를 새 UI 상태로 반영한다`() {
        val initialRecord = createRecord(id = 1L, title = "기존 제목")
        val viewModel = TripRecordsViewModel(
            locations = locations,
            initialRecords = TripRecords(listOf(initialRecord)),
        )

        viewModel.onAction(TripRecordAction.StartEditing(initialRecord.id))
        assertEquals(TripRecordEffect.OpenEditor, viewModel.uiState.effect)
        viewModel.onAction(TripRecordAction.EffectHandled)
        viewModel.onAction(TripRecordAction.TitleChanged("수정된 제목"))
        viewModel.onAction(TripRecordAction.Save)

        assertEquals("수정된 제목", viewModel.uiState.records.single().title)
        val openDetail = assertIs<TripRecordEffect.OpenDetail>(viewModel.uiState.effect)
        assertEquals(initialRecord.id, openDetail.recordId)
        assertTrue(openDetail.replaceCurrent)

        viewModel.onAction(TripRecordAction.EffectHandled)
        viewModel.onAction(TripRecordAction.Delete(initialRecord.id))

        assertTrue(viewModel.uiState.records.isEmpty())
        assertEquals(TripRecordEffect.CloseDetail, viewModel.uiState.effect)
    }

    @Test
    fun `장소와 검색어 필터는 전체 기록을 바꾸지 않고 표시 목록만 변경한다`() {
        val seoulRecord = createRecord(id = 1L, title = "서울 산책", location = "강남구")
        val busanRecord = createRecord(id = 2L, title = "바다 여행", location = "부산광역시")
        val viewModel = TripRecordsViewModel(
            locations = locations,
            initialRecords = TripRecords(listOf(seoulRecord, busanRecord)),
        )

        viewModel.onAction(TripRecordAction.LocationFilterChanged(seoul.id))
        assertEquals(listOf(seoulRecord.id), viewModel.uiState.visibleRecords.map { it.id })

        viewModel.onAction(TripRecordAction.KeywordChanged("없는 기록"))
        assertTrue(viewModel.uiState.visibleRecords.isEmpty())
        assertEquals(2, viewModel.uiState.records.size)

        viewModel.onAction(TripRecordAction.KeywordChanged("산책"))
        assertEquals(listOf(seoulRecord.id), viewModel.uiState.visibleRecords.map { it.id })
    }

    @Test
    fun `잘못된 편집 값은 도메인에 전달하지 않고 오류 상태로 남긴다`() {
        val viewModel = TripRecordsViewModel(locations)
        viewModel.onAction(TripRecordAction.StartCreating(gangnam))
        viewModel.onAction(TripRecordAction.EffectHandled)
        viewModel.onAction(TripRecordAction.TitleChanged("잘못된 여행"))
        viewModel.onAction(TripRecordAction.StartDateChanged("2026-08-03"))
        viewModel.onAction(TripRecordAction.EndDateChanged("2026-08-01"))

        viewModel.onAction(TripRecordAction.Save)

        assertTrue(viewModel.uiState.records.isEmpty())
        assertEquals("종료일은 시작일보다 빠를 수 없습니다.", viewModel.uiState.editor.errorMessage)
        assertFalse(viewModel.uiState.editor.isSaving)
        assertNull(viewModel.uiState.effect)
    }

    private fun createRecord(
        id: Long,
        title: String,
        location: String = "강남구",
    ): TripRecord = TripRecord(
        id = id,
        imageUrl = "",
        tripRecordTitle = title,
        tripRecordDescription = "여행 기록",
        startTripDate = LocalDate(2026, 8, 1),
        endTripDate = LocalDate(2026, 8, 3),
        location = location,
    )

    companion object {
        private val seoul = Location(1, 1, null, "KR-11", "서울특별시", LocationType.PROVINCE)
        private val gangnam = Location(2, 1, 1, "11680", "강남구", LocationType.DISTRICT)
        private val busan = Location(3, 1, null, "KR-26", "부산광역시", LocationType.PROVINCE)
        private val locations = listOf(seoul, gangnam, busan)
    }
}
