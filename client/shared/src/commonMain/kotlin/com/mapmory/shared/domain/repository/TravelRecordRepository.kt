package com.mapmory.shared.domain.repository

import com.mapmory.shared.domain.model.TravelRecord
import com.mapmory.shared.domain.model.TravelRecordDraft
import com.mapmory.shared.domain.model.TravelRecordPage
import com.mapmory.shared.domain.model.TravelRecordQuery

// 여행 기록 데이터를 조회하고 변경하는 도메인 계약
interface TravelRecordRepository {
    // 조건에 맞는 여행 기록 목록을 페이지 단위로 가져온다.
    suspend fun getTravelRecords(query: TravelRecordQuery): Result<TravelRecordPage>

    // ID로 여행 기록 하나를 가져온다.
    suspend fun getTravelRecord(id: Long): Result<TravelRecord>

    // 새 여행 기록을 저장한다.
    suspend fun createTravelRecord(draft: TravelRecordDraft): Result<TravelRecord>

    // 기존 여행 기록을 수정한다.
    suspend fun updateTravelRecord(id: Long, draft: TravelRecordDraft): Result<TravelRecord>

    // 여행 기록을 삭제한다.
    suspend fun deleteTravelRecord(id: Long): Result<Unit>
}
