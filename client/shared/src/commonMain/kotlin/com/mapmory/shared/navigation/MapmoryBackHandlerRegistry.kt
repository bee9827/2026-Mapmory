package com.mapmory.shared.navigation

internal class MapmoryBackHandlerRegistry {
    private val registrations = mutableListOf<Registration>()

    fun register(handler: () -> Boolean): Registration {
        val registration = Registration(handler)
        registrations += registration
        return registration
    }

    fun unregister(registration: Registration) {
        registrations.remove(registration)
    }

    fun handleBack(): Boolean = registrations.lastOrNull()?.handler?.invoke() == true

    class Registration internal constructor(
        internal val handler: () -> Boolean,
    )
}
