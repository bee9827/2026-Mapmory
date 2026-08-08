package com.mapmory.shared.data.remote

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

class LocationRemoteRepositoryTest {
    @Test
    fun getLocationsMapsServerLocationAndQuery() = runBlocking {
        val client = HttpClient(MockEngine) {
            configureCommonHttpClient()
            engine {
                addHandler { request ->
                    assertEquals("1", request.url.parameters["countryId"])
                    assertEquals("11", request.url.parameters["parentId"])
                    respond(
                        content = ByteReadChannel(
                            """{"data":[{"id":101,"countryId":1,"parentId":11,"regionCode":"11110","name":"종로구","locationType":"DISTRICT"}]}""",
                        ),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }
            }
        }

        val result = LocationRemoteRepository(client, "https://api.example.com/api/v1")
            .getLocations(countryId = 1, parentId = 11)
            .getOrThrow()

        assertEquals(101, result.single().id)
        assertEquals("11110", result.single().regionCode)
        client.close()
    }
}
