package com.mapmory.shared.presentation.map.data

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

class KoreaMapRemoteDataSourceTest {
    @Test
    fun loadsBoundariesAndFiltersDistrictsByProvinceCode() = runBlocking {
        val client = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    val body = if (request.url.toString().contains("provinces")) {
                        """
                        {
                          "features": [{
                            "properties": {"code": "32", "name": "강원도"},
                            "geometry": {"type": "Polygon", "coordinates": [[[127, 38], [128, 38], [128, 37], [127, 38]]]}
                          }]
                        }
                        """
                    } else {
                        """
                        {
                          "features": [
                            {
                              "properties": {"code": "32340", "name": "평창군"},
                              "geometry": {"type": "MultiPolygon", "coordinates": [[[[127, 38], [128, 38], [128, 37], [127, 38]]]]}
                            },
                            {
                              "properties": {"code": "11", "name": "서울특별시"},
                              "geometry": {"type": "Polygon", "coordinates": [[[126, 38], [127, 38], [127, 37], [126, 38]]]}
                            }
                          ]
                        }
                        """
                    }
                    respond(
                        content = ByteReadChannel(body),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }
            }
        }

        val result = KoreaMapRemoteDataSource(client).load().getOrThrow()

        assertEquals("32", result.provinces.single().code)
        assertEquals(listOf("32340"), result.districtsFor("KR-42").map { it.code })
        assertEquals(1, result.districtsFor("11").size)
        client.close()
    }
}
