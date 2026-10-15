package com.mapmory.shared.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

internal class NavigationGestureMotion {
    var isActive by androidx.compose.runtime.mutableStateOf(false)
}

internal val LocalNavigationGestureMotion =
    androidx.compose.runtime.staticCompositionLocalOf { NavigationGestureMotion() }
