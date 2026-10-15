package com.mapmory.shared.presentation.map.data

import com.mapmory.shared.domain.model.KoreanSelectableDistrictCodes
import com.mapmory.shared.presentation.map.domain.GeoPoint
import com.mapmory.shared.presentation.map.ui.regionAt
import com.mapmory.shared.presentation.map.ui.alignDistrictFillWithProvince
import com.mapmory.shared.runSuspend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KoreaDistrictOverviewResourceTest {
    @Test
    fun `전국_채움은_정규_시군구_코드와_성남시_접합부를_유지한다`() = runSuspend {
        val districts = GeneratedKoreaDistrictMapData.overviewDistricts()

        assertEquals(230, districts.size)
        assertEquals(KoreanSelectableDistrictCodes.map { it.code }.toSet(), districts.map { it.code }.toSet())
        assertTrue(districts.all { district -> district.rings.isNotEmpty() })
        assertEquals("41130", districts.regionAt(GeoPoint(127.121f, 37.413f))?.code)
    }

    @Test
    fun `성남시_북쪽의_채움도_기존_경기도_외곽선까지_채운다`() = runSuspend {
        val district = GeneratedKoreaDistrictMapData.overviewDistricts().single { it.code == "41130" }
        val province = GeneratedKoreaMapData.provinces.single { it.code == "KR-41" }
        val lines = GeneratedKoreaDistrictMapData.overviewData().lines
        val sharedEdges = lines.flatMap { it.zipWithNext { start, end -> setOf(start, end) } }.toSet()
        val aligned = district.copy(rings = alignDistrictFillWithProvince(district.rings, province.rings, sharedEdges))

        assertEquals("41130", listOf(aligned).regionAt(GeoPoint(127.13487f, 37.47115f))?.code)
        val originalEdges = district.rings.flatMap { it.zipWithNext { start, end -> setOf(start, end) } }.toSet()
        val alignedEdges = aligned.rings.flatMap { it.zipWithNext { start, end -> setOf(start, end) } }.toSet()
        assertTrue(originalEdges.intersect(sharedEdges).all { it in alignedEdges })
    }

    @Test
    fun `시도_외곽에_채움을_맞춰도_모든_시군_공유_경계는_유지한다`() = runSuspend {
        val overview = GeneratedKoreaDistrictMapData.overviewData()
        val sharedEdges = overview.lines.flatMap { it.zipWithNext { start, end -> setOf(start, end) } }.toSet()
        val provinceCodes = overview.labels.associate { it.code to it.provinceCode }
        val provinces = GeneratedKoreaMapData.provinces.associateBy { it.code }
        GeneratedKoreaDistrictMapData.overviewDistricts().filter { it.code != "27720" }.forEach { district ->
            val province = provinces.getValue(provinceCodes.getValue(district.code))
            val aligned = alignDistrictFillWithProvince(district.rings, province.rings, sharedEdges)
            val originalEdges = district.rings.flatMap { it.zipWithNext { start, end -> setOf(start, end) } }.toSet()
            val alignedEdges = aligned.flatMap { it.zipWithNext { start, end -> setOf(start, end) } }.toSet()
            assertTrue(originalEdges.intersect(sharedEdges).all { it in alignedEdges }, district.code)
            assertTrue(aligned.all { it.size >= 4 && it.first() == it.last() }, district.code)
        }
    }
}
