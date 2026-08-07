package com.mapmory.shared.domain

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class TripRecordsTest {
    @Test
    fun `여행 기록을 추가한다`() {
        val tripRecords = TripRecords()

        val result = tripRecords.addTripRecord(
            imageUri = "image.jpg",
            tripRecordTitle = "제주도 여행",
            tripRecordDescription = "성산일출봉을 다녀왔다",
            tripLocation = "제주도",
            startTripDate = LocalDate(2026, 8, 1),
            endTripDate = LocalDate(2026, 8, 3),
        )

        assertEquals(0, tripRecords.tripRecords.size)
        assertEquals(1, result.tripRecords.size)
        assertEquals("제주도 여행", result.tripRecords.single().tripRecordTitle)
    }

    @Test
    fun `시작일이 종료일보다 늦은 여행 기록은 추가할 수 없다`() {
        val tripRecords = TripRecords()

        assertFailsWith<IllegalArgumentException> {
            tripRecords.addTripRecord(
                imageUri = "image.jpg",
                tripRecordTitle = "잘못된 여행",
                tripRecordDescription = null,
                tripLocation = "제주도",
                startTripDate = LocalDate(2026, 8, 8),
                endTripDate = LocalDate(2026, 8, 7),
            )
        }
        assertEquals(emptyList(), tripRecords.tripRecords)
    }

    @Test
    fun `여행 기록을 삭제한다`() {
        val record = createTripRecord()
        val tripRecords = TripRecords(listOf(record))

        val result = tripRecords.removeTripRecord(record)

        assertEquals(emptyList(), result.tripRecords)
    }

    @Test
    fun `존재하지 않는 여행 기록을 삭제하면 기존 객체를 반환한다`() {
        val tripRecords = TripRecords(listOf(createTripRecord(id = 1L)))

        val result = tripRecords.removeTripRecord(createTripRecord(id = 2L))

        assertSame(tripRecords, result)
    }

    @Test
    fun `빈 목록에서 여행 기록을 삭제하면 기존 객체를 반환한다`() {
        val tripRecords = TripRecords()

        val result = tripRecords.removeTripRecord(createTripRecord())

        assertSame(tripRecords, result)
    }

    @Test
    fun `내용이 달라도 ID가 같은 여행 기록을 삭제한다`() {
        val storedRecord = createTripRecord(id = 1L, title = "저장된 제목")
        val deletingRecord = createTripRecord(id = 1L, title = "변경된 제목")
        val tripRecords = TripRecords(listOf(storedRecord))

        val result = tripRecords.removeTripRecord(deletingRecord)

        assertEquals(emptyList(), result.tripRecords)
    }

    @Test
    fun `여행 기록의 전달된 필드만 수정한다`() {
        val record = createTripRecord()
        val tripRecords = TripRecords(listOf(record))

        val result = tripRecords.editTripRecord(
            editingRecord = record,
            editingImage = null,
            editingTitle = "수정된 제목",
            editingDescription = null,
            editingLocation = "부산",
        )

        val editedRecord = result.tripRecords.single()
        assertNotSame(tripRecords, result)
        assertEquals(record.id, editedRecord.id)
        assertEquals(record.imageUrl, editedRecord.imageUrl)
        assertEquals("수정된 제목", editedRecord.tripRecordTitle)
        assertEquals(record.tripRecordDescription, editedRecord.tripRecordDescription)
        assertEquals("부산", editedRecord.location)
        assertEquals(record.startTripDate, editedRecord.startTripDate)
        assertEquals(record.endTripDate, editedRecord.endTripDate)
    }

    @Test
    fun `여행 기록을 수정해도 목록 순서는 유지된다`() {
        val firstRecord = createTripRecord(id = 1L, title = "첫 번째")
        val secondRecord = createTripRecord(id = 2L, title = "두 번째")
        val tripRecords = TripRecords(listOf(firstRecord, secondRecord))

        val result = tripRecords.editTripRecord(
            editingRecord = firstRecord,
            editingImage = null,
            editingTitle = "수정된 첫 번째",
            editingDescription = null,
            editingLocation = null,
        )

        assertEquals(listOf(firstRecord.id, secondRecord.id), result.tripRecords.map { it.id })
        assertEquals("수정된 첫 번째", result.tripRecords.first().tripRecordTitle)
    }

    @Test
    fun `수정할 필드가 없으면 기록 내용을 유지한다`() {
        val record = createTripRecord()
        val tripRecords = TripRecords(listOf(record))

        val result = tripRecords.editTripRecord(
            editingRecord = record,
            editingImage = null,
            editingTitle = null,
            editingDescription = null,
            editingLocation = null,
        )

        assertEquals(record, result.tripRecords.single())
        assertEquals(record, tripRecords.tripRecords.single())
    }

    @Test
    fun `존재하지 않는 여행 기록을 수정하면 예외가 발생한다`() {
        val tripRecords = TripRecords(listOf(createTripRecord(id = 1L)))

        assertFailsWith<IllegalArgumentException> {
            tripRecords.editTripRecord(
                editingRecord = createTripRecord(id = 2L),
                editingImage = null,
                editingTitle = "수정된 제목",
                editingDescription = null,
                editingLocation = null,
            )
        }
    }

    @Test
    fun `생성자에 전달한 목록이 바뀌어도 여행 기록 목록은 바뀌지 않는다`() {
        val source = mutableListOf(createTripRecord(id = 1L))
        val tripRecords = TripRecords(source)

        source += createTripRecord(id = 2L)

        assertEquals(listOf(1L), tripRecords.tripRecords.map { it.id })
    }

    private fun createTripRecord(
        id: Long = 1L,
        title: String = "제주도 여행",
    ): TripRecord = TripRecord(
        id = id,
        imageUrl = "image.jpg",
        tripRecordTitle = title,
        tripRecordDescription = "여행 기록",
        startTripDate = LocalDate(2026, 8, 1),
        endTripDate = LocalDate(2026, 8, 3),
        location = "제주도",
    )
}
