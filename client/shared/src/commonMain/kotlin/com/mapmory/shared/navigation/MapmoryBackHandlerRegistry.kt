package com.mapmory.shared.navigation

internal class MapmoryBackHandlerRegistry {
    private val registrations = mutableListOf<Registration>()

    fun register(ownerId: String, handler: () -> Boolean): Registration {
        val registration = Registration(ownerId, handler)
        registrations += registration
        return registration
    }

    fun unregister(registration: Registration) {
        registrations.remove(registration)
    }

    fun handleBack(ownerId: String?): Boolean {
        return registrations
            .lastOrNull { registration -> registration.ownerId == ownerId }
            ?.handler
            ?.invoke() == true
    }

    class Registration internal constructor(
        internal val ownerId: String,
        internal val handler: () -> Boolean,
    )
}
