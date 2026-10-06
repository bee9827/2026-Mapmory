package com.mapmory.shared.navigation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MapmoryBackHandlerRegistryTest {
    @Test
    fun `only the current back stack owner's handler receives system back`() {
        val registry = MapmoryBackHandlerRegistry()
        var hiddenMapWasCalled = false
        var editorWasCalled = false
        registry.register("map") {
            hiddenMapWasCalled = true
            true
        }
        registry.register("editor") {
            editorWasCalled = true
            true
        }

        assertTrue(registry.handleBack("editor"))
        assertTrue(editorWasCalled)
        assertFalse(hiddenMapWasCalled)
    }

    @Test
    fun `missing current owner falls through to navigator`() {
        val registry = MapmoryBackHandlerRegistry()
        registry.register("map") { true }

        assertFalse(registry.handleBack("records"))
    }
}
