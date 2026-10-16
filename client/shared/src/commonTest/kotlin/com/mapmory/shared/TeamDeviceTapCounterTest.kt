package com.mapmory.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class TeamDeviceTapCounterTest {
    @Test
    fun `일곱번째_탭마다_전환하고_다시_센다`() {
        val counter = TeamDeviceTapCounter()

        val results = List(14) { counter.tap() }

        assertEquals(listOf(6, 13), results.indices.filter { results[it] })
    }
}
