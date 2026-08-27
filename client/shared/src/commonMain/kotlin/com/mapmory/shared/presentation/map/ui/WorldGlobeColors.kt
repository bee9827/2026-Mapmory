package com.mapmory.shared.presentation.map.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
internal data class WorldGlobeColors(
    val background: Color,
    val outerGlow: Color,
    val sphereGradientCenter: Color,
    val sphereGradientMiddle: Color,
    val sphereGradientEdge: Color,
    val visitedFill: Color,
    val unvisitedFill: Color,
    val visitedOutline: Color,
    val unvisitedOutline: Color,
    val highlightCenter: Color,
    val highlightMiddle: Color,
)

private val LightWorldGlobeColors = WorldGlobeColors(
    background = Color(0xFFFAFCFB),
    outerGlow = Color(0xFF789587).copy(alpha = 0.08f),
    sphereGradientCenter = Color(0xFFF8FAF8),
    sphereGradientMiddle = Color(0xFFF0F4F1),
    sphereGradientEdge = Color(0xFFE7EDE9),
    visitedFill = Color(0xFF4D9272),
    unvisitedFill = Color(0xFFDCE4DF),
    visitedOutline = Color(0xFF2F7659).copy(alpha = 0.72f),
    unvisitedOutline = Color(0xFFB8C6BD).copy(alpha = 0.72f),
    highlightCenter = Color.White.copy(alpha = 0.34f),
    highlightMiddle = Color.White.copy(alpha = 0.12f),
)

private val DarkWorldGlobeColors = WorldGlobeColors(
    background = Color(0xFF121518),
    outerGlow = Color(0xFF7F9ABA).copy(alpha = 0.055f),
    sphereGradientCenter = Color(0xFF2A3747),
    sphereGradientMiddle = Color(0xFF1B2533),
    sphereGradientEdge = Color(0xFF111923),
    visitedFill = Color(0xFF35C987),
    unvisitedFill = Color(0xFF2B3546),
    visitedOutline = Color(0xFF8AEBC1).copy(alpha = 0.82f),
    unvisitedOutline = Color(0xFF7C8FAA).copy(alpha = 0.54f),
    highlightCenter = Color.White.copy(alpha = 0.09f),
    highlightMiddle = Color.White.copy(alpha = 0.025f),
)

internal fun worldGlobeColors(isDark: Boolean): WorldGlobeColors =
    if (isDark) DarkWorldGlobeColors else LightWorldGlobeColors
