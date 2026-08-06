package com.mapmory.shared.data.repository

import com.mapmory.shared.domain.model.RecordMedia
import com.mapmory.shared.domain.model.TravelRecord
import com.mapmory.shared.domain.model.TravelRecordDraft
import com.mapmory.shared.domain.model.TravelRecordPage
import com.mapmory.shared.domain.model.TravelRecordQuery
import com.mapmory.shared.domain.repository.TravelRecordRepository

/** 서버 API를 연결하기 전 기록 흐름을 확인하는 메모리 기반 구현이다. */
class FakeTravelRecordRepository(
    private val memberId: Long,
    private val now: () -> String,
) : TravelRecordRepository {
    private val records = mutableListOf<TravelRecord>()
    private var nextRecordId = 1L
    private var nextMediaId = 1L

    override suspend fun getTravelRecords(query: TravelRecordQuery): Result<TravelRecordPage> {
        if (query.page < 0 || query.size <= 0) {
            return Result.failure(IllegalArgumentException("페이지 번호와 크기를 확인해 주세요."))
        }

        val filteredRecords = records.filter { record ->
            (query.locationId == null || record.locationId == query.locationId) &&
                (query.keyword.isNullOrBlank() ||
                    record.title.contains(query.keyword, ignoreCase = true) ||
                    record.content.contains(query.keyword, ignoreCase = true))
        }
        val totalPages = (filteredRecords.size + query.size - 1) / query.size
        val pageRecords = filteredRecords.drop(query.page * query.size).take(query.size)

        return Result.success(
            TravelRecordPage(
                records = pageRecords,
                page = query.page,
                size = query.size,
                totalElements = filteredRecords.size.toLong(),
                totalPages = totalPages,
            ),
        )
    }

    override suspend fun getTravelRecord(id: Long): Result<TravelRecord> =
        records.find { it.id == id }?.let(Result.Companion::success)
            ?: Result.failure(NoSuchElementException("여행 기록을 찾을 수 없습니다."))

    override suspend fun createTravelRecord(draft: TravelRecordDraft): Result<TravelRecord> {
        val timestamp = now()
        val record = TravelRecord(
            id = nextRecordId++,
            memberId = memberId,
            locationId = draft.locationId,
            title = draft.title,
            content = draft.content,
            startDate = draft.startDate,
            endDate = draft.endDate ?: draft.startDate,
            media = createMedia(draft.mediaObjectKeys),
            createdAt = timestamp,
            updatedAt = timestamp,
        )
        records += record
        return Result.success(record)
    }

    override suspend fun updateTravelRecord(id: Long, draft: TravelRecordDraft): Result<TravelRecord> {
        val index = records.indexOfFirst { it.id == id }
        if (index == -1) return Result.failure(NoSuchElementException("여행 기록을 찾을 수 없습니다."))

        val updatedRecord = records[index].copy(
            locationId = draft.locationId,
            title = draft.title,
            content = draft.content,
            startDate = draft.startDate,
            endDate = draft.endDate ?: draft.startDate,
            media = createMedia(draft.mediaObjectKeys),
            updatedAt = now(),
        )
        records[index] = updatedRecord
        return Result.success(updatedRecord)
    }

    override suspend fun deleteTravelRecord(id: Long): Result<Unit> {
        if (!records.removeAll { it.id == id }) {
            return Result.failure(NoSuchElementException("여행 기록을 찾을 수 없습니다."))
        }
        return Result.success(Unit)
    }

    private fun createMedia(objectKeys: List<String>): List<RecordMedia> =
        objectKeys.mapIndexed { index, objectKey ->
            RecordMedia(
                id = nextMediaId++,
                objectKey = objectKey,
                sortOrder = index,
                url = null,
            )
        }
}
