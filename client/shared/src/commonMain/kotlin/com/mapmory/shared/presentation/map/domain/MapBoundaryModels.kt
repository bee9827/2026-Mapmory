package com.mapmory.shared.presentation.map.domain

data class MapBoundaryData(
    val provinces: List<ProvincePolygon>,
    val districts: List<ProvincePolygon>,
) {
    fun districtsFor(provinceCode: String): List<ProvincePolygon> = districts.filter { district ->
        district.code.startsWith(geoJsonProvinceCodeForServer(provinceCode))
    }
}

private val geoJsonProvinceCodesByServerCode = mapOf(
    "11" to "11",
    "26" to "21",
    "27" to "22",
    "28" to "23",
    "29" to "24",
    "30" to "25",
    "31" to "26",
    "41" to "31",
    "42" to "32",
    "43" to "33",
    "44" to "34",
    "45" to "35",
    "46" to "36",
    "47" to "37",
    "48" to "38",
    "49" to "39",
    "50" to "29",
)

private val serverProvinceCodesByGeoJsonCode =
    geoJsonProvinceCodesByServerCode.entries.associate { (serverCode, geoJsonCode) ->
        geoJsonCode to serverCode
    }

fun geoJsonProvinceCodeForServer(serverCode: String): String =
    geoJsonProvinceCodesByServerCode[serverCode.removePrefix("KR-")] ?: serverCode.removePrefix("KR-")

fun serverProvinceCodeForGeoJson(geoJsonCode: String): String =
    serverProvinceCodesByGeoJsonCode[geoJsonCode] ?: geoJsonCode
