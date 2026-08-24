package com.mapmory.shared.data.local

import com.mapmory.shared.domain.model.LocationType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StaticRegionCatalogTest {
    private val catalog = StaticRegionCatalog()

    @Test
    fun `canonical province and district codes resolve without App composable`() {
        val seoul = catalog.findByCode("KR-11")
        val gangnam = catalog.findDistrict(
            provinceCode = "KR-11",
            districtCode = "11680",
        )

        assertEquals("서울특별시", seoul?.name)
        assertEquals(LocationType.PROVINCE, seoul?.type)
        assertEquals("강남구", gangnam?.name)
        assertEquals(seoul?.id, gangnam?.parentId)
    }

    @Test
    fun `district lookup never crosses the requested province`() {
        assertNull(
            catalog.findDistrict(
                provinceCode = "KR-26",
                districtCode = "11680",
            ),
        )
    }
}
