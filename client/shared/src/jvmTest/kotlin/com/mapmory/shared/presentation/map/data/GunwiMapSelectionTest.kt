package com.mapmory.shared.presentation.map.data

import com.mapmory.shared.presentation.map.domain.GeoPoint
import com.mapmory.shared.presentation.map.ui.regionAt
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class GunwiMapSelectionTest {
    @Test
    fun `군위와_대구_중심은_동일한_대구_시도_경계로_선택한다`() = runBlocking {
        val provinces = GeneratedKoreaMapData.provinces
        assertEquals("27720", GeneratedKoreaDistrictMapData.forProvince("KR-27")
            .single { it.code == "27720" }.code)

        assertEquals("KR-27", provinces.regionAt(GeoPoint(128.57f, 36.24f))?.code)
        assertEquals("KR-27", provinces.regionAt(GeoPoint(128.60f, 35.87f))?.code)
        assertEquals("KR-47", provinces.regionAt(GeoPoint(128.69f, 36.35f))?.code)
    }
}
