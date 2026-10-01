package com.mapmory.shared.app

import com.mapmory.shared.domain.model.TripRecordData
import com.mapmory.shared.domain.model.TripRecordDraft
import com.mapmory.shared.domain.model.TripRecordPage
import com.mapmory.shared.domain.model.TripRecordQuery
import com.mapmory.shared.domain.repository.TripRecordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackgroundTripRecordSaverTest {
    @Test
    fun `첫_저장_실패_후_한번_자동_재시도한다`() = runBlocking {
        var attempts = 0
        var changedCount = 0
        val repository = object : EmptyTripRecordRepository() {
            override suspend fun createTripRecord(draft: TripRecordDraft): Result<TripRecordData> {
                attempts += 1
                return if (attempts == 1) Result.failure(IllegalStateException("일시 오류"))
                else Result.success(draft.toRecord())
            }
        }
        val saver = BackgroundTripRecordSaver(
            repository = repository,
            scope = CoroutineScope(coroutineContext),
            onSaved = { changedCount += 1 },
            automaticRetryDelayMillis = 0,
        )

        saver.enqueue(draft())
        saver.saves.first { saves -> saves.isEmpty() && attempts > 0 }

        assertEquals(2, attempts)
        assertEquals(1, changedCount)
    }

    @Test
    fun `두번_실패하면_알리고_사용자가_다시_시도할_수_있다`() = runBlocking {
        var attempts = 0
        val notifications = mutableListOf<String>()
        val repository = object : EmptyTripRecordRepository() {
            override suspend fun createTripRecord(draft: TripRecordDraft): Result<TripRecordData> {
                attempts += 1
                return if (attempts <= 2) Result.failure(IllegalStateException("네트워크 오류"))
                else Result.success(draft.toRecord())
            }
        }
        val saver = BackgroundTripRecordSaver(
            repository = repository,
            scope = CoroutineScope(coroutineContext),
            failureNotifier = BackgroundSaveFailureNotifier(notifications::add),
            automaticRetryDelayMillis = 0,
        )

        val id = saver.enqueue(draft())
        val failed = saver.saves.first { saves ->
            saves.singleOrNull()?.status == BackgroundSaveStatus.FAILED
        }.single()

        assertEquals(id, failed.id)
        assertEquals(2, attempts)
        assertTrue(notifications.isNotEmpty())

        saver.retry(id)
        saver.saves.first { saves -> saves.isEmpty() }
        assertEquals(3, attempts)
    }
}

private open class EmptyTripRecordRepository : TripRecordRepository {
    override suspend fun getTripRecords(query: TripRecordQuery): Result<TripRecordPage> =
        Result.failure(UnsupportedOperationException())

    override suspend fun getTripRecord(id: Long): Result<TripRecordData> =
        Result.failure(UnsupportedOperationException())

    override suspend fun createTripRecord(draft: TripRecordDraft): Result<TripRecordData> =
        Result.failure(UnsupportedOperationException())

    override suspend fun updateTripRecord(
        id: Long,
        draft: TripRecordDraft,
    ): Result<TripRecordData> = Result.failure(UnsupportedOperationException())

    override suspend fun deleteTripRecord(id: Long): Result<Unit> =
        Result.failure(UnsupportedOperationException())
}

private fun draft() = TripRecordDraft(
    locationId = 1,
    title = "백그라운드 기록",
    content = null,
    startDate = "2026-10-02",
    endDate = null,
    mediaObjectKeys = emptyList(),
)

private fun TripRecordDraft.toRecord() = TripRecordData(
    id = 101,
    locationId = locationId,
    title = title,
    content = content.orEmpty(),
    startDate = startDate,
    endDate = endDate,
    media = emptyList(),
    createdAt = "2026-10-02T00:00:00",
    updatedAt = "2026-10-02T00:00:00",
)
