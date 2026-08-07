package com.mapmory.shared.data.remote

import com.mapmory.shared.data.remote.model.ApiErrorDto
import com.mapmory.shared.data.remote.model.ApiResponseDto
import com.mapmory.shared.data.remote.model.IdResponseDto
import com.mapmory.shared.data.remote.model.PageDto
import com.mapmory.shared.data.remote.model.TravelRecordDetailDto
import com.mapmory.shared.data.remote.model.TravelRecordListItemDto
import com.mapmory.shared.data.remote.model.toDomain
import com.mapmory.shared.data.remote.model.toRequestDto
import com.mapmory.shared.domain.model.TravelRecord
import com.mapmory.shared.domain.model.TravelRecordDraft
import com.mapmory.shared.domain.model.TravelRecordPage
import com.mapmory.shared.domain.model.TravelRecordQuery
import com.mapmory.shared.domain.repository.TravelRecordRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse

class TravelRecordRemoteRepository(
    private val client: HttpClient,
    baseUrl: String,
    private val memberId: Long,
) : TravelRecordRepository {
    private val recordsUrl = "${baseUrl.trimEnd('/')}/travel-records"

    override suspend fun getTravelRecords(query: TravelRecordQuery): Result<TravelRecordPage> =
        runCatching {
            val response = client.get(recordsUrl) {
                memberHeader()
                query.locationId?.let { parameter("locationId", it) }
                query.keyword?.takeIf(String::isNotBlank)?.let { parameter("keyword", it) }
                parameter("page", query.page)
                parameter("size", query.size)
            }.requireSuccess()

            response.body<ApiResponseDto<PageDto<TravelRecordListItemDto>>>().data.toDomain()
        }

    override suspend fun getTravelRecord(id: Long): Result<TravelRecord> =
        runCatching {
            client.get("$recordsUrl/$id") {
                memberHeader()
            }.requireSuccess()
                .body<ApiResponseDto<TravelRecordDetailDto>>()
                .data
                .toDomain()
        }

    override suspend fun createTravelRecord(draft: TravelRecordDraft): Result<TravelRecord> =
        runCatching {
            val id = client.post(recordsUrl) {
                memberHeader()
                setBody(draft.toRequestDto())
            }.requireSuccess()
                .body<ApiResponseDto<IdResponseDto>>()
                .data
                .id

            getTravelRecord(id).getOrThrow()
        }

    override suspend fun updateTravelRecord(id: Long, draft: TravelRecordDraft): Result<TravelRecord> =
        runCatching {
            client.put("$recordsUrl/$id") {
                memberHeader()
                setBody(draft.toRequestDto())
            }.requireSuccess()

            getTravelRecord(id).getOrThrow()
        }

    override suspend fun deleteTravelRecord(id: Long): Result<Unit> =
        runCatching {
            client.delete("$recordsUrl/$id") {
                memberHeader()
            }.requireSuccess()
        }

    private fun io.ktor.client.request.HttpRequestBuilder.memberHeader() {
        header("X-Member-Id", memberId)
    }
}

private suspend fun HttpResponse.requireSuccess(): HttpResponse {
    if (status.value in 200..299) return this

    val error = runCatching { body<ApiErrorDto>() }.getOrNull()
    throw TravelRecordApiException(
        statusCode = status.value,
        code = error?.code ?: "HTTP_${status.value}",
        message = error?.message ?: "API 요청에 실패했습니다.",
    )
}

class TravelRecordApiException(
    val statusCode: Int,
    val code: String,
    override val message: String,
) : IllegalStateException(message)
