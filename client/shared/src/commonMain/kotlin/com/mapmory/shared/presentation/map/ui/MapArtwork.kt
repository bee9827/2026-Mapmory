package com.mapmory.shared.presentation.map.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapmory.shared.presentation.map.domain.MapScope

@Composable
fun MapArtwork(
    scope: MapScope = MapScope.WORLD,
    visitedCountryCodes: Set<String> = emptySet(),
    visitedRegionCodes: Set<String> = emptySet(),
    modifier: Modifier = Modifier,
) {
    when (scope) {
        MapScope.WORLD -> WorldGlobe(
            visitedCountryCodes = visitedCountryCodes,
            modifier = modifier,
        )

        MapScope.KOREA -> KoreaMapArtwork(visitedRegionCodes = visitedRegionCodes, modifier = modifier)
    }
}
