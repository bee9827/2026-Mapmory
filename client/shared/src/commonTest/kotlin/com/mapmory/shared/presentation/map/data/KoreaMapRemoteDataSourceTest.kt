package com.mapmory.shared.presentation.map.data

import com.mapmory.shared.domain.model.KoreanSelectableDistrictCodes
import com.mapmory.shared.presentation.map.domain.GeoPoint
import com.mapmory.shared.presentation.map.domain.MapBoundaryData
import com.mapmory.shared.presentation.map.domain.ProvincePolygon
import com.mapmory.shared.presentation.map.ui.regionAt
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

    @Test
    fun groupsCityDistrictsButKeepsMetropolitanDistricts() {
        val data = MapBoundaryData(
            provinces = emptyList(),
            districts = listOf(
                polygon("31111", "수원시장안구"),
                polygon("31113", "수원시권선구"),
                polygon("31220", "평택시"),
                polygon("33111", "청주시상당구"),
                polygon("33112", "청주시서원구"),
                polygon("33130", "충주시"),
                polygon("11110", "종로구"),
                polygon("11140", "중구"),
            ),
        )

        assertEquals(
            listOf("수원시", "평택시"),
            data.displayDistrictsFor("KR-41").map(ProvincePolygon::name),
        )
        assertEquals(
            listOf("41110", "41220"),
            data.displayDistrictsFor("KR-41").map(ProvincePolygon::code),
        )
        assertEquals(2, data.displayDistrictsFor("KR-41").first().rings.size)
        assertEquals(
            listOf("청주시", "충주시"),
            data.displayDistrictsFor("KR-43").map(ProvincePolygon::name),
        )
        assertEquals(
            listOf("종로구", "중구"),
            data.displayDistrictsFor("KR-11").map(ProvincePolygon::name),
        )
    }

    @Test
    fun selectsTheSmallestExactBoundaryWhenRegionsOverlap() {
        val province = square("KR-41", "경기도", 0f, 0f, 10f, 10f)
        val city = square("KR-11", "서울특별시", 2f, 2f, 4f, 4f)

        assertEquals("KR-11", listOf(province, city).regionAt(GeoPoint(3f, 3f))?.code)
        assertEquals("KR-41", listOf(province, city).regionAt(GeoPoint(1f, 1f))?.code)
        assertEquals(null, listOf(province, city).regionAt(GeoPoint(11f, 11f)))
    }

    @Test
    fun distinguishesSeoulFromGyeonggiOnTheGeneratedMap() {
        assertEquals(
            "KR-11",
            GeneratedKoreaMapData.provinces.regionAt(GeoPoint(126.98f, 37.56f))?.code,
        )
        assertEquals(
            "KR-41",
            GeneratedKoreaMapData.provinces.regionAt(GeoPoint(127.20f, 37.40f))?.code,
        )
    }

    @Test
    fun exposesOrdinaryCitiesAsSelectableLocations() {
        assertEquals(
            listOf("41110" to "경기도 수원시"),
            KoreanSelectableDistrictCodes
                .filter { it.code.startsWith("4111") }
                .map { it.code to it.name },
        )
        assertEquals(
            listOf("27110", "27140", "27170", "27200", "27230", "27260", "27290", "27710", "27720"),
            KoreanSelectableDistrictCodes
                .filter { it.provinceCode == "KR-27" }
                .map { it.code },
        )
    }

    private fun polygon(code: String, name: String): ProvincePolygon = ProvincePolygon(
        code = code,
        name = name,
        rings = listOf(
            listOf(
                GeoPoint(0f, 0f),
                GeoPoint(1f, 0f),
                GeoPoint(0f, 1f),
            ),
        ),
    )

    private fun square(
        code: String,
        name: String,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): ProvincePolygon = ProvincePolygon(
        code = code,
        name = name,
        rings = listOf(
            listOf(
                GeoPoint(left, top),
                GeoPoint(right, top),
                GeoPoint(right, bottom),
                GeoPoint(left, bottom),
            ),
        ),
    )
}
