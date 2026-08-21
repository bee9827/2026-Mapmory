package com.mapmory.shared.presentation.map.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertEquals

class KoreaMapPanBoundsTest {
    @Test
    fun `map smaller than viewport stays at its initial position`() {
        val result = clampKoreaMapPan(
            pan = Offset(500f, -500f),
            zoom = 1f,
            viewportSize = IntSize(1000, 800),
            mapLeft = 100f,
            mapTop = 100f,
            mapWidth = 800f,
            mapHeight = 600f,
        )

        assertEquals(0f, result.x, 0.001f)
        assertEquals(0f, result.y, 0.001f)
    }

    @Test
    fun `zoomed map cannot be dragged completely outside viewport`() {
        val result = clampKoreaMapPan(
            pan = Offset(10_000f, -10_000f),
            zoom = 2f,
            viewportSize = IntSize(1000, 800),
            mapLeft = 100f,
            mapTop = 100f,
            mapWidth = 800f,
            mapHeight = 600f,
        )

        assertEquals(300f, result.x, 0.001f)
        assertEquals(-200f, result.y, 0.001f)
    }
}
