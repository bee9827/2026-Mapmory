package com.mapmory.shared.data.remote

import com.mapmory.shared.domain.model.TravelRecordQuery
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class TravelRecordRemoteRepositoryTest {
    @Test
    fun getRecordsSendsContractQueryAndMemberHeader() = runBlocking {
        val client = HttpClient(MockEngine) {
            configureCommonHttpClient()
            engine {
                addHandler { request ->
                    assertEquals("GET", request.method.value)
                    assertEquals("10", request.headers["X-Member-Id"])
                    assertEquals("1", request.url.parameters["locationId"])
                    assertEquals("0", request.url.parameters["page"])
                    assertEquals("20", request.url.parameters["size"])
                    respond(
                        content = ByteReadChannel("""{"data":{"items":[],"page":0,"size":20,"totalElements":0,"totalPages":0,"hasNext":false}}"""),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }
            }
        }

        val result = TravelRecordRemoteRepository(client, "https://api.example.com/api/v1", 10)
            .getTravelRecords(TravelRecordQuery(locationId = 1))

        assertEquals(0, result.getOrThrow().records.size)
        client.close()
    }
}
