package com.mapmory.shared.data.remote

import com.mapmory.shared.data.remote.model.ApiErrorDto
import com.mapmory.shared.data.remote.model.ApiResponseDto
import com.mapmory.shared.data.remote.model.IdResponseDto
import com.mapmory.shared.data.remote.model.PageDto
import com.mapmory.shared.data.remote.model.TripRecordDetailDto
import com.mapmory.shared.data.remote.model.TripRecordListItemDto
import com.mapmory.shared.data.remote.model.toDomain
import com.mapmory.shared.data.remote.model.toRequestDto
import com.mapmory.shared.domain.model.TripRecordData
import com.mapmory.shared.domain.model.TripRecordDraft
import com.mapmory.shared.domain.model.TripRecordPage
import com.mapmory.shared.domain.model.TripRecordQuery
import com.mapmory.shared.domain.repository.TripRecordRepository
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

class TripRecordRemoteRepository(
    private val client: HttpClient,
    baseUrl: String,
    private val memberId: Long,
) : TripRecordRepository {
    private val recordsUrl = "${baseUrl.trimEnd('/')}/travel-records"

    override suspend fun getTripRecords(query: TripRecordQuery): Result<TripRecordPage> =
        runCatching {
            val response = client.get(recordsUrl) {
                memberHeader()
                query.locationId?.let { parameter("locationId", it) }
                query.keyword?.takeIf(String::isNotBlank)?.let { parameter("keyword", it) }
                parameter("page", query.page)
                parameter("size", query.size)
            }.requireSuccess()

            response.body<ApiResponseDto<PageDto<TripRecordListItemDto>>>().data.toDomain()
        }

    override suspend fun getTripRecord(id: Long): Result<TripRecordData> =
        runCatching {
            client.get("$recordsUrl/$id") {
                memberHeader()
            }.requireSuccess()
                .body<ApiResponseDto<TripRecordDetailDto>>()
                .data
                .toDomain()
        }

    override suspend fun createTripRecord(draft: TripRecordDraft): Result<TripRecordData> =
        runCatching {
            val id = client.post(recordsUrl) {
                memberHeader()
                setBody(draft.toRequestDto())
            }.requireSuccess()
                .body<ApiResponseDto<IdResponseDto>>()
                .data
                .id

            getTripRecord(id).getOrThrow()
        }

    override suspend fun updateTripRecord(id: Long, draft: TripRecordDraft): Result<TripRecordData> =
        runCatching {
            client.put("$recordsUrl/$id") {
                memberHeader()
                setBody(draft.toRequestDto())
            }.requireSuccess()

            getTripRecord(id).getOrThrow()
        }

    override suspend fun deleteTripRecord(id: Long): Result<Unit> =
        runCatching {
            client.delete("$recordsUrl/$id") {
                memberHeader()
            }.requireSuccess()
        }

    private fun io.ktor.client.request.HttpRequestBuilder.memberHeader() {
        header("X-Member-Id", memberId)
    }
}

internal suspend fun HttpResponse.requireSuccess(): HttpResponse {
    if (status.value in 200..299) return this

    val error = runCatching { body<ApiErrorDto>() }.getOrNull()
    throw TripRecordApiException(
        statusCode = status.value,
        code = error?.code ?: "HTTP_${status.value}",
        message = error?.message ?: "API 요청에 실패했습니다.",
    )
}

class TripRecordApiException(
    val statusCode: Int,
    val code: String,
    override val message: String,
) : IllegalStateException(message)
