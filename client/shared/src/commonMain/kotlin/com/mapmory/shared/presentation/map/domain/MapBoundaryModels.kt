package com.mapmory.shared.presentation.map.domain

data class MapBoundaryData(
    val provinces: List<ProvincePolygon>,
    val districts: List<ProvincePolygon>,
) {
    fun districtsFor(provinceCode: String): List<ProvincePolygon> = districts.filter { district ->
        district.code.startsWith(geoJsonProvinceCodeForServer(provinceCode))
    }

    /** 프로토타입의 지도 단계에 맞춘 표시 단위다. */
    fun displayDistrictsFor(provinceCode: String): List<ProvincePolygon> {
        val province = provinceCode.removePrefix("KR-")
        val districts = districtsFor(provinceCode).map(ProvincePolygon::withServerCode)
        if (province in MetropolitanProvinceCodes) return districts

        return districts
            .groupBy { district -> district.displayName() }
            .map { (name, groupedDistricts) ->
                if (groupedDistricts.size == 1) {
                    groupedDistricts.single()
                } else {
                    ProvincePolygon(
                        code = groupedDistricts.first().code.dropLast(1) + "0",
                        name = name,
                        rings = groupedDistricts.flatMap(ProvincePolygon::rings),
                    )
                }
            }
    }

}

private fun ProvincePolygon.displayName(): String {
    val cityName = CityWithDistrictPattern.find(name)?.groupValues?.get(1)
    return cityName ?: name
}

private fun ProvincePolygon.withServerCode(): ProvincePolygon = copy(
    code = serverProvinceCodeForGeoJson(code.take(2)) + code.drop(2),
)

private val CityWithDistrictPattern = Regex("^(.+시).+구$")

private val MetropolitanProvinceCodes = setOf(
    "11", // 서울특별시
    "26", // 부산광역시
    "27", // 대구광역시
    "28", // 인천광역시
    "29", // 광주광역시
    "30", // 대전광역시
    "31", // 울산광역시
    "50", // 세종특별자치시
)

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
