package com.mapmory.shared.presentation.map.ui

import com.mapmory.shared.presentation.map.domain.GeoPoint
import kotlin.test.Test
import kotlin.test.assertEquals

class KoreaDistrictFillGeometryTest {
    private fun point(x: Float, y: Float) = GeoPoint(x, y)
    private val seam = setOf(point(0f, 1f), point(2f, 1f))
    private val province = listOf(
        point(0f, 0f), point(2f, 0f), point(2f, 1f), point(2f, 2f),
        point(1f, 2f), point(0f, 2f), point(0f, 1f), point(0f, 0f),
    )

    @Test
    fun `노출된_채움만_기존_시도_외곽선에_맞추고_공유_경계는_유지한다`() {
        val original = listOf(point(0f, 1f), point(2f, 1f), point(2f, 1.995f), point(0f, 1.995f), point(0f, 1f))

        val aligned = alignDistrictFillWithProvince(listOf(original), listOf(province.reversed()), setOf(seam)).single()

        assertEquals(listOf(point(0f, 1f), point(2f, 1f), point(2f, 2f), point(1f, 2f), point(0f, 2f), point(0f, 1f)), aligned)
        assertEquals(1.995f, original[2].latitude)
    }

    @Test
    fun `끝점만_일치하는_먼_외곽선으로_채움을_넓히지_않는다`() {
        val original = listOf(point(0f, 1f), point(2f, 1f), point(2f, 1.5f), point(0f, 1.5f), point(0f, 1f))
        assertEquals(listOf(original), alignDistrictFillWithProvince(listOf(original), listOf(province), setOf(seam)))
    }

    @Test
    fun `공유_경계가_없는_떨어진_섬은_그대로_유지한다`() {
        val island = listOf(point(3f, 3f), point(4f, 3f), point(3f, 4f), point(3f, 3f))
        assertEquals(listOf(island), alignDistrictFillWithProvince(listOf(island), listOf(province), setOf(seam)))
    }
}
