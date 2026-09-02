package com.mapmory.shared.data.remote

import com.mapmory.shared.data.remote.model.ProblemFieldErrorDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame

class ApiCallTest {
    @Test
    fun cancellationExceptionIsRethrown() = runBlocking {
        val cancellation = CancellationException("요청 취소")

        val thrown = assertFailsWith<CancellationException> {
            apiCall<Unit> { throw cancellation }
        }

        assertEquals(cancellation, thrown)
    }

    @Test
    fun regularExceptionIsReturnedAsFailure() = runBlocking {
        val result = apiCall<Unit> { throw IllegalStateException("요청 실패") }

        assertIs<IllegalStateException>(result.exceptionOrNull())
        Unit
    }

    @Test
    fun `DNS 오류를 사용자 친화적인 연결 안내로 변환한다`() = runBlocking {
        val rawError = IllegalStateException(
            "Unable to resolve host \"api.map-mory.com\": No address associated with hostname",
        )

        val result = apiCall<Unit> { throw rawError }.exceptionOrNull()

        assertIs<MapmoryConnectionException>(result)
        assertEquals(
            "서버에 연결할 수 없습니다. 인터넷 연결을 확인한 뒤 잠시 후 다시 시도해 주세요.",
            result.message,
        )
        assertSame(rawError, result.cause)
    }

    @Test
    fun `시간 초과 오류를 재시도 안내로 변환한다`() {
        val rawError = IllegalStateException("request timed out")

        val result = rawError.toUserFriendlyRemoteFailure()

        assertIs<MapmoryConnectionException>(result)
        assertEquals(
            "서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해 주세요.",
            result.message,
        )
    }

    @Test
    fun `서버가 제공한 API 오류 문구는 유지한다`() {
        val apiError = MapmoryApiException(
            statusCode = 400,
            code = "VALIDATION_ERROR",
            title = "요청 값이 올바르지 않습니다.",
            detail = "제목을 확인해 주세요.",
            instance = "/api/v1/travel-records",
            errors = listOf(ProblemFieldErrorDto("title", "제목을 확인해 주세요.")),
        )

        assertSame(apiError, apiError.toUserFriendlyRemoteFailure())
    }

    @Test
    fun `알 수 없는 내부 오류는 임의로 네트워크 오류로 바꾸지 않는다`() {
        val error = IllegalArgumentException("unexpected response shape")

        assertSame(error, error.toUserFriendlyRemoteFailure())
    }
}
