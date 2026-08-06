package com.mapmory.shared.domain.usecase

import com.mapmory.shared.domain.model.TravelRecord
import com.mapmory.shared.domain.model.TravelRecordDraft
import com.mapmory.shared.domain.model.TravelRecordPage
import com.mapmory.shared.domain.model.TravelRecordQuery
import com.mapmory.shared.domain.repository.TravelRecordRepository

// 여행 기록 목록 조회를 담당한다.
class GetTravelRecordsUseCase(
    private val repository: TravelRecordRepository,
) {
    suspend operator fun invoke(query: TravelRecordQuery = TravelRecordQuery()): Result<TravelRecordPage> =
        repository.getTravelRecords(query)
}

// 여행 기록 상세 조회를 담당한다.
class GetTravelRecordUseCase(
    private val repository: TravelRecordRepository,
) {
    suspend operator fun invoke(id: Long): Result<TravelRecord> = repository.getTravelRecord(id)
}

// 여행 기록 생성을 담당한다.
class CreateTravelRecordUseCase(
    private val repository: TravelRecordRepository,
) {
    suspend operator fun invoke(draft: TravelRecordDraft): Result<TravelRecord> =
        repository.createTravelRecord(draft)
}

// 여행 기록 수정을 담당한다.
class UpdateTravelRecordUseCase(
    private val repository: TravelRecordRepository,
) {
    suspend operator fun invoke(id: Long, draft: TravelRecordDraft): Result<TravelRecord> =
        repository.updateTravelRecord(id, draft)
}

// 여행 기록 삭제를 담당한다.
class DeleteTravelRecordUseCase(
    private val repository: TravelRecordRepository,
) {
    suspend operator fun invoke(id: Long): Result<Unit> = repository.deleteTravelRecord(id)
}
