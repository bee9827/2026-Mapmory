package com.mapmory.shared.presentation.components

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

internal actual fun photoExpansionTransition(isOpening: Boolean): ContentTransform =
    if (isOpening) {
        // The retained thumbnail is already visible, so avoid fading through the black viewer.
        EnterTransition.None togetherWith ExitTransition.None
    } else {
        // On close, remove the fullscreen chrome before the shared photo settles into the grid.
        fadeIn(tween(durationMillis = 100, delayMillis = 100)) togetherWith
            fadeOut(tween(durationMillis = 100))
    }

@Composable
internal actual fun photoViewerBackground(): Modifier = Modifier.background(Color.Black)
