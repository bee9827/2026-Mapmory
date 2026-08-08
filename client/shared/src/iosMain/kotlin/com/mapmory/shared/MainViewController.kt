package com.mapmory.shared

import androidx.compose.ui.window.ComposeUIViewController
import mapmoryclient.shared.generated.resources.Res
import mapmoryclient.shared.generated.resources.globe_hero
import org.jetbrains.compose.resources.painterResource

fun MainViewController() = ComposeUIViewController {
    MapmoryApp(
        globePainter = { painterResource(Res.drawable.globe_hero) },
    )
}
