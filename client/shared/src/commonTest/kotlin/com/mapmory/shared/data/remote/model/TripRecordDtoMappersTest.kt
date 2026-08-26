package com.mapmory.shared.data.remote.model

import com.mapmory.shared.data.local.StaticRegionCatalog
import com.mapmory.shared.domain.model.TripRecordDraft
import com.mapmory.shared.domain.model.TripRecordQuery
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ApiDtoMappersTest {
    private val catalog = StaticRegionCatalog()

    @Test
    fun detailRegionCodesAndObjectKeysMapToLocalDomain() {
        val result = TripRecordDetailDto(
            id = 101,
            title = "비 오는 날의 제주시",
            content = "골목을 걸었다.",
            region = TripRecordRegionDto(
                country = RegionCodeDto("KR", "대한민국"),
                province = RegionCodeDto("49", "제주특별자치도"),
                district = RegionCodeDto("50110", "제주시"),
            ),
            startDate = "2026-08-11",
            endDate = null,
            objectKeys = listOf("travel-records/guest/a.jpg"),
            createdAt = "2026-08-14T10:30:00",
            updatedAt = "2026-08-15T09:00:00",
        ).toDomain(catalog)

        assertEquals(catalog.requireByCode("50110").id, result.locationId)
        assertEquals("travel-records/guest/a.jpg", result.media.single().objectKey)
        assertEquals(0, result.media.single().sortOrder)
    }

    @Test
    fun koreanDistrictDraftMapsToServerCodePath() {
        val jejuCity = catalog.requireByCode("50110")

        val request = TripRecordDraft(
            locationId = jejuCity.id,
            title = "제주 여행",
            content = "본문",
            startDate = "2026-08-11",
            endDate = null,
            mediaObjectKeys = listOf("travel-records/guest/a.jpg"),
        ).toRequestDto(catalog)

        assertEquals("KR", request.countryCode)
        assertEquals("49", request.provinceCode)
        assertEquals("50110", request.districtCode)
        assertEquals(listOf("travel-records/guest/a.jpg"), request.objectKeys)
    }

    @Test
    fun foreignDraftMapsOnlyCountryCode() {
        val japan = catalog.requireByCode("JP")

        val request = TripRecordDraft(
            locationId = japan.id,
            title = "일본 여행",
            content = "",
            startDate = "2026-08-11",
            endDate = null,
            mediaObjectKeys = emptyList(),
        ).toRequestDto(catalog)

        assertEquals("JP", request.countryCode)
        assertEquals(null, request.provinceCode)
        assertEquals(null, request.districtCode)
    }

    @Test
    fun createRequestRejectsMissingStartDateBeforeNetworkCall() {
        val japan = catalog.requireByCode("JP")

        assertFailsWith<IllegalArgumentException> {
            TripRecordDraft(
                locationId = japan.id,
                title = "일본 여행",
                content = "",
                startDate = null,
                endDate = null,
                mediaObjectKeys = emptyList(),
            ).toRequestDto(catalog)
        }
    }

    @Test
    fun provinceListFilterMapsToCountryAndProvinceCodes() {
        val jejuProvince = catalog.requireByCode("KR-49")

        val path = TripRecordQuery(locationId = jejuProvince.id).toRegionQuery(catalog)

        assertEquals("KR", path?.countryCode)
        assertEquals("49", path?.provinceCode)
        assertEquals(null, path?.districtCode)
    }

    @Test
    fun koreanRecordRequiresDistrictAndTitleIsLimitedToTwoHundredCharacters() {
        val seoul = catalog.requireByCode("KR-11")
        val japan = catalog.requireByCode("JP")

        assertFailsWith<IllegalArgumentException> {
            draft(locationId = seoul.id, title = "서울 여행").toRequestDto(catalog)
        }
        assertFailsWith<IllegalArgumentException> {
            draft(locationId = japan.id, title = "가".repeat(201)).toRequestDto(catalog)
        }
    }

    private fun draft(locationId: Long, title: String) = TripRecordDraft(
        locationId = locationId,
        title = title,
        content = "",
        startDate = "2026-08-11",
        endDate = null,
        mediaObjectKeys = emptyList(),
    )
}
