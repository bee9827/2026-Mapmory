package com.mapmory.shared

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController(onThemeChanged: (Boolean) -> Unit) = ComposeUIViewController {
    MapmoryApp(
        contentWindowInsets = WindowInsets.safeDrawing,
        onThemeChanged = onThemeChanged,
    )
}
