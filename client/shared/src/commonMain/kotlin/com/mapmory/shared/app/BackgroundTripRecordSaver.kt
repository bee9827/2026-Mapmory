package com.mapmory.shared.app

import com.mapmory.shared.domain.model.TripRecordDraft
import com.mapmory.shared.domain.repository.TripRecordRepository
import com.mapmory.shared.domain.repository.ProgressReportingTripRecordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class BackgroundSaveStatus {
    QUEUED,
    SAVING,
    FAILED,
}

data class BackgroundTripRecordSave(
    val id: Long,
    val title: String,
    val locationName: String,
    val startDate: String,
    val photoCount: Int,
    val status: BackgroundSaveStatus,
    val progressPercent: Int = 0,
    val errorMessage: String? = null,
)

interface BackgroundSaveExecution {
    suspend fun <T> run(block: suspend () -> T): T
}

object DirectBackgroundSaveExecution : BackgroundSaveExecution {
    override suspend fun <T> run(block: suspend () -> T): T = block()
}

class BackgroundTripRecordSaver internal constructor(
    private val repository: TripRecordRepository,
    private val scope: CoroutineScope,
    private val execution: BackgroundSaveExecution = DirectBackgroundSaveExecution,
    private val onSaved: () -> Unit = {},
    private val automaticRetryDelayMillis: Long = AutomaticRetryDelayMillis,
) {
    private val queueMutex = Mutex()
    private val drafts = mutableMapOf<Long, TripRecordDraft>()
    private val mutableSaves = MutableStateFlow<List<BackgroundTripRecordSave>>(emptyList())
    private var nextId = 0L

    val saves: StateFlow<List<BackgroundTripRecordSave>> = mutableSaves.asStateFlow()

    fun enqueue(draft: TripRecordDraft, locationName: String): Long {
        val id = ++nextId
        val compactDraft = draft.copy(
            localMedia = draft.localMedia.map { media ->
                media.copy(previewBytes = null, originalBytes = null)
            },
        )
        drafts[id] = compactDraft
        mutableSaves.update { current ->
            current + BackgroundTripRecordSave(
                id = id,
                title = compactDraft.title.ifBlank { "제목 없는 기록" },
                locationName = locationName,
                startDate = compactDraft.startDate,
                photoCount = compactDraft.mediaObjectKeys.size,
                status = BackgroundSaveStatus.QUEUED,
            )
        }
        launch(id)
        return id
    }

    fun retry(id: Long) {
        if (drafts[id] == null) return
        val failed = mutableSaves.value.firstOrNull { save -> save.id == id } ?: return
        if (failed.status != BackgroundSaveStatus.FAILED) return
        mutableSaves.update { current ->
            current.map { save ->
                if (save.id == id) {
                    save.copy(
                        status = BackgroundSaveStatus.QUEUED,
                        progressPercent = 0,
                        errorMessage = null,
                    )
                }
                else save
            }
        }
        launch(id)
    }

    fun dismissFailure(id: Long) {
        if (mutableSaves.value.none { it.id == id && it.status == BackgroundSaveStatus.FAILED }) return
        drafts.remove(id)
        mutableSaves.update { current -> current.filterNot { save -> save.id == id } }
    }

    private fun launch(id: Long) {
        scope.launch {
            queueMutex.withLock {
                save(id)
            }
        }
    }

    private suspend fun save(id: Long) {
        val draft = drafts[id] ?: return
        mutableSaves.update { current ->
            current.map { save ->
                if (save.id == id) {
                    save.copy(
                        status = BackgroundSaveStatus.SAVING,
                        progressPercent = 0,
                        errorMessage = null,
                    )
                }
                else save
            }
        }

        val result = execution.run {
            var latest = create(id, draft)
            if (latest.isFailure) {
                delay(automaticRetryDelayMillis)
                updateProgress(id, 0)
                latest = create(id, draft)
            }
            latest
        }

        result.fold(
            onSuccess = {
                drafts.remove(id)
                mutableSaves.update { current -> current.filterNot { save -> save.id == id } }
                onSaved()
            },
            onFailure = { error ->
                val message = error.message ?: DefaultFailureMessage
                mutableSaves.update { current ->
                    current.map { save ->
                        if (save.id == id) {
                            save.copy(status = BackgroundSaveStatus.FAILED, errorMessage = message)
                        } else {
                            save
                        }
                    }
                }
            },
        )
    }

    private suspend fun create(id: Long, draft: TripRecordDraft) =
        (repository as? ProgressReportingTripRecordRepository)?.createTripRecord(draft) { progress ->
            updateProgress(id, progress)
        } ?: repository.createTripRecord(draft)

    private fun updateProgress(id: Long, progress: Int) {
        mutableSaves.update { current ->
            current.map { save ->
                if (save.id == id) save.copy(progressPercent = progress.coerceIn(0, 100)) else save
            }
        }
    }
}

private const val AutomaticRetryDelayMillis = 1_000L
private const val DefaultFailureMessage = "기록 저장에 실패했어요. 앱에서 다시 시도해 주세요."
