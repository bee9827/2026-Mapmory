package com.mapmory.shared.presentation.map.data

import com.mapmory.shared.presentation.map.domain.GeoPoint
import com.mapmory.shared.presentation.map.ui.overviewRegionAt
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class GunwiMapSelectionTest {
    @Test
    fun `군위_영역은_옛_경북_윤곽보다_먼저_선택한다`() = runBlocking {
        val provinces = GeneratedKoreaMapData.provinces
        val gunwi = GeneratedKoreaDistrictMapData.forProvince("KR-27")
            .single { it.code == "27720" }

        assertEquals("27720", provinces.overviewRegionAt(GeoPoint(128.57f, 36.24f), gunwi)?.code)
        assertEquals("KR-27", provinces.overviewRegionAt(GeoPoint(128.60f, 35.87f), gunwi)?.code)
        assertEquals("KR-47", provinces.overviewRegionAt(GeoPoint(128.69f, 36.35f), gunwi)?.code)
    }
}
