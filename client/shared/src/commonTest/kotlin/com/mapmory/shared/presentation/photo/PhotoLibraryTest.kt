package com.mapmory.shared.presentation.photo

import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.LocationType
import kotlin.test.Test
import kotlin.test.assertEquals

class PhotoLibraryTest {
    @Test
    fun selectedPhotosAreDeduplicatedAndLimited() {
        val existing = listOf(photo("same"), photo("existing"))
        val incoming = listOf(photo("same")) + (1..20).map { photo("new-$it") }

        val merged = mergeSelectedPhotos(existing, incoming)

        assertEquals(MaxPhotosPerRecord, merged.size)
        assertEquals(1, merged.count { it.id == "same" })
        assertEquals(listOf("same", "existing"), merged.take(2).map(SelectedPhoto::id))
    }

    @Test
    fun koreanDistrictSearchIncludesParentAndCountry() {
        val district = Location(
            id = 2,
            countryId = 1,
            parentId = 1,
            regionCode = "11680",
            name = "강남구",
            type = LocationType.DISTRICT,
        )

        assertEquals("강남구 서울특별시 대한민국", district.recommendationSearchText("서울특별시"))
        assertEquals(100_000.0, district.recommendationRadiusMeters())
        assertEquals(
            true,
            PhotoAdministrativeArea(
                countryCode = "KR",
                administrativeArea = "서울특별시",
                subAdministrativeArea = null,
                locality = "서울특별시",
                subLocality = "강남구",
            ).matches(district, "서울특별시"),
        )
        assertEquals(
            false,
            PhotoAdministrativeArea(
                countryCode = "KR",
                administrativeArea = "서울특별시",
                subAdministrativeArea = null,
                locality = "서울특별시",
                subLocality = "서초구",
            ).matches(district, "서울특별시"),
        )
    }

    @Test
    fun fullKoreanDistrictNameMatchesSeparatedGeocoderFields() {
        val district = Location(
            id = 3,
            countryId = 1,
            parentId = 11,
            regionCode = "41111",
            name = "경기도 수원시장안구",
            type = LocationType.DISTRICT,
        )
        val photoArea = PhotoAdministrativeArea(
            countryCode = "KR",
            administrativeArea = "경기도",
            subAdministrativeArea = "수원시",
            locality = "수원시",
            subLocality = "장안구",
        )

        assertEquals(true, photoArea.matches(district, "경기도"))
        assertEquals("경기도 수원시장안구 대한민국", district.recommendationSearchText("경기도"))
    }

    @Test
    fun koreanDistrictMatchesWhenGeocoderPutsDistrictInAnyAddressField() {
        val district = Location(
            id = 4,
            countryId = 1,
            parentId = 1,
            regionCode = "11620",
            name = "서울특별시 관악구",
            type = LocationType.DISTRICT,
        )

        assertEquals(
            true,
            PhotoAdministrativeArea(
                countryCode = "KR",
                administrativeArea = "서울특별시 관악구",
                subAdministrativeArea = null,
                locality = null,
                subLocality = "신림동",
            ).matches(district, "서울특별시"),
        )
    }

    @Test
    fun koreanDistrictAllowsMissingDistrictSuffixInGeocoderResult() {
        val district = Location(
            id = 5,
            countryId = 1,
            parentId = 1,
            regionCode = "11680",
            name = "강남구",
            type = LocationType.DISTRICT,
        )

        assertEquals(
            true,
            PhotoAdministrativeArea(
                countryCode = "KR",
                administrativeArea = "서울특별시",
                subAdministrativeArea = null,
                locality = null,
                subLocality = "강남",
            ).matches(district, "서울특별시"),
        )
    }

    private fun photo(id: String) = SelectedPhoto(
        id = id,
        displayName = "$id.jpg",
        previewBytes = null,
    )
}
