package com.mapmory.shared

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.window.ComposeUIViewController
import com.mapmory.shared.app.createGuestRemoteAppContainer
import com.mapmory.shared.data.auth.IosAuthTokenStore

fun MainViewController(
    onThemeChanged: (Boolean) -> Unit,
) = createGuestRemoteAppContainer(
    tokenStore = IosAuthTokenStore(),
).let { container ->
    ComposeUIViewController {
        DisposableEffect(container) {
            onDispose(container::close)
        }

        MapmoryApp(
            container = container,
            contentWindowInsets = WindowInsets.safeDrawing,
            onThemeChanged = onThemeChanged,
        )
    }
}