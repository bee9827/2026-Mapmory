package com.mapmory.shared.presentation.photo

import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.LocationType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking

class PhotoRecommendationRegionDeviceTest {
    @Test
    fun canonicalDistrictCodeLoadsItsBundledBoundary() = runBlocking {
        val gangnam = Location(
            id = 11680L,
            countryId = 1L,
            parentId = 1L,
            regionCode = "11680",
            name = "강남구",
            type = LocationType.DISTRICT,
        )

        assertEquals("11680", gangnam.photoRecommendationRegion()?.code)
    }

    @Test
    fun unknownDistrictCodeDoesNotUseAnotherBoundary() = runBlocking {
        val unknownDistrict = Location(
            id = 11999L,
            countryId = 1L,
            parentId = 1L,
            regionCode = "11999",
            name = "알 수 없는 구",
            type = LocationType.DISTRICT,
        )

        assertEquals(null, unknownDistrict.photoRecommendationRegion())
    }
}
