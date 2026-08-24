package com.mapmory.shared.data.repository

import com.mapmory.shared.domain.model.TripRecordData
import com.mapmory.shared.domain.model.TripRecordDraft
import com.mapmory.shared.domain.model.TripRecordMedia
import com.mapmory.shared.domain.model.TripRecordPage
import com.mapmory.shared.domain.model.TripRecordQuery
import com.mapmory.shared.domain.model.dateValidationError
import com.mapmory.shared.domain.repository.TripRecordRepository

/** 서버 API를 연결하기 전 기록 흐름을 확인하는 메모리 기반 구현이다. */
class FakeTripRecordRepository(
    private val memberId: Long,
    private val now: () -> String,
) : TripRecordRepository {
    private val records = mutableListOf<TripRecordData>()
    private var nextRecordId = 1L
    private var nextMediaId = 1L

    override suspend fun getTripRecords(query: TripRecordQuery): Result<TripRecordPage> {
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
            TripRecordPage(
                records = pageRecords,
                page = query.page,
                size = query.size,
                totalElements = filteredRecords.size.toLong(),
                totalPages = totalPages,
            ),
        )
    }

    override suspend fun getTripRecord(id: Long): Result<TripRecordData> =
        records.find { it.id == id }?.let(Result.Companion::success)
            ?: Result.failure(NoSuchElementException("여행 기록을 찾을 수 없습니다."))

    override suspend fun createTripRecord(draft: TripRecordDraft): Result<TripRecordData> {
        draft.dateValidationError()?.let { return Result.failure(IllegalArgumentException(it)) }
        val timestamp = now()
        val record = TripRecordData(
            id = nextRecordId++,
            memberId = memberId,
            locationId = draft.locationId,
            title = draft.title,
            content = draft.content,
            startDate = draft.startDate,
            endDate = draft.endDate,
            media = createMedia(draft),
            createdAt = timestamp,
            updatedAt = timestamp,
        )
        records += record
        return Result.success(record)
    }

    override suspend fun updateTripRecord(id: Long, draft: TripRecordDraft): Result<TripRecordData> {
        val index = records.indexOfFirst { it.id == id }
        if (index == -1) return Result.failure(NoSuchElementException("여행 기록을 찾을 수 없습니다."))
        draft.dateValidationError()?.let { return Result.failure(IllegalArgumentException(it)) }

        val updatedRecord = records[index].copy(
            locationId = draft.locationId,
            title = draft.title,
            content = draft.content,
            startDate = draft.startDate,
            endDate = draft.endDate,
            media = createMedia(draft),
            updatedAt = now(),
        )
        records[index] = updatedRecord
        return Result.success(updatedRecord)
    }

    override suspend fun deleteTripRecord(id: Long): Result<Unit> {
        if (!records.removeAll { it.id == id }) {
            return Result.failure(NoSuchElementException("여행 기록을 찾을 수 없습니다."))
        }
        return Result.success(Unit)
    }

    private fun createMedia(draft: TripRecordDraft): List<TripRecordMedia> {
        val localMediaByObjectKey = draft.localMedia.associateBy { it.objectKey }
        return draft.mediaObjectKeys.mapIndexed { index, objectKey ->
            val localMedia = localMediaByObjectKey[objectKey]
            TripRecordMedia(
                id = nextMediaId++,
                objectKey = objectKey,
                sortOrder = localMedia?.sortOrder ?: index,
                url = null,
                previewBytes = localMedia?.previewBytes?.copyOf(),
                originalBytes = localMedia?.originalBytes?.copyOf(),
                latitude = localMedia?.latitude,
                longitude = localMedia?.longitude,
                capturedAt = localMedia?.capturedAt,
            )
        }
    }
}
