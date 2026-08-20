package com.mapmory.shared.presentation.map.data

import com.mapmory.shared.presentation.map.domain.GeoPoint
import com.mapmory.shared.presentation.map.domain.MapBoundaryData
import com.mapmory.shared.presentation.map.domain.ProvincePolygon
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive

class KoreaMapRemoteDataSource(
    private val client: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    suspend fun load(): Result<MapBoundaryData> = runCatching {
        val provinces = getJson(ProvincesUrl)
            .let { json.decodeFromString<GeoJsonFeatureCollectionDto>(it) }
            .features
            .mapNotNull { it.toPolygon() }

        val districts = getJson(MunicipalitiesUrl)
            .let { json.decodeFromString<GeoJsonFeatureCollectionDto>(it) }
            .features
            .mapNotNull { it.toPolygon() }

        require(provinces.isNotEmpty()) { "시·도 지도 데이터를 찾을 수 없습니다." }
        require(districts.isNotEmpty()) { "시·군·구 지도 데이터를 찾을 수 없습니다." }
        MapBoundaryData(provinces = provinces, districts = districts)
    }

    private suspend fun getJson(url: String): String {
        val response = client.get(url)
        require(response.status.value in 200..299) { "지도 데이터 요청에 실패했습니다." }
        return response.bodyAsText()
    }

    private fun GeoJsonFeatureDto.toPolygon(): ProvincePolygon? {
        val geometry = geometry ?: return null
        val code = properties.stringValue("code") ?: return null
        val name = properties.stringValue("name") ?: code
        val rings = geometry.coordinates.toRings(geometry.type)
        return ProvincePolygon(code = code, name = name, rings = rings).takeIf { rings.isNotEmpty() }
    }
}

@Serializable
private data class GeoJsonFeatureCollectionDto(
    val features: List<GeoJsonFeatureDto> = emptyList(),
)

@Serializable
private data class GeoJsonFeatureDto(
    val geometry: GeoJsonGeometryDto? = null,
    val properties: JsonObject = buildJsonObject {},
)

@Serializable
private data class GeoJsonGeometryDto(
    val type: String,
    val coordinates: JsonElement,
)

private fun JsonObject.stringValue(key: String): String? = this[key]?.jsonPrimitive?.content

private fun JsonElement.toRings(type: String): List<List<GeoPoint>> = when (type) {
    "Polygon" -> jsonArray().mapNotNull(JsonElement::toRing)
    "MultiPolygon" -> jsonArray()
        .flatMap { polygon -> polygon.jsonArray().mapNotNull(JsonElement::toRing) }
    else -> emptyList()
}

private fun JsonElement.toRing(): List<GeoPoint>? = jsonArrayOrNull()
    ?.mapNotNull { point ->
        val values = point.jsonArrayOrNull() ?: return@mapNotNull null
        if (values.size < 2) return@mapNotNull null
        GeoPoint(
            longitude = values[0].jsonPrimitive.floatOrNull() ?: return@mapNotNull null,
            latitude = values[1].jsonPrimitive.floatOrNull() ?: return@mapNotNull null,
        )
    }
    ?.takeIf { it.size >= 3 }

private fun JsonElement.jsonArray(): JsonArray = jsonArrayOrNull()
    ?: error("GeoJSON 좌표 형식이 올바르지 않습니다.")

private fun JsonElement.jsonArrayOrNull(): JsonArray? = this as? JsonArray

private fun kotlinx.serialization.json.JsonPrimitive.floatOrNull(): Float? = content.toFloatOrNull()

private const val ProvincesUrl =
    "https://raw.githubusercontent.com/southkorea/southkorea-maps/master/kostat/2018/json/skorea-provinces-2018-geo.json"
private const val MunicipalitiesUrl =
    "https://raw.githubusercontent.com/southkorea/southkorea-maps/master/kostat/2018/json/skorea-municipalities-2018-geo.json"
