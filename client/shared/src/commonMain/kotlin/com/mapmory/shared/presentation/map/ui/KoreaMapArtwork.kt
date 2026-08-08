package com.mapmory.shared.presentation.map.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.mapmory.shared.presentation.map.data.GeneratedKoreaMapData
import com.mapmory.shared.presentation.map.domain.GeoPoint
import com.mapmory.shared.presentation.map.domain.ProvincePolygon
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min

@Composable
fun KoreaMapArtwork(
    visitedRegionCodes: Set<String> = emptySet(),
    modifier: Modifier = Modifier,
) {
    val provinces = remember { GeneratedKoreaMapData.provinces }
    val bounds = remember(provinces) { KoreaBounds.from(provinces) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07171B)),
    ) {
        val longitudeSpan = bounds.maxLongitude - bounds.minLongitude
        val latitudeSpan = bounds.maxLatitude - bounds.minLatitude
        if (longitudeSpan <= 0f || latitudeSpan <= 0f) return@Canvas

        // Longitude degrees are physically shorter than latitude degrees in Korea.
        // Applying the center-latitude factor keeps the province silhouettes from
        // looking stretched horizontally while retaining the source coordinates.
        val referenceLatitudeRadians =
            (bounds.minLatitude + bounds.maxLatitude) / 2f * PI.toFloat() / 180f
        val longitudeFactor = cos(referenceLatitudeRadians).coerceAtLeast(0.1f)
        val projectedLongitudeSpan = longitudeSpan * longitudeFactor

        val horizontalPadding = size.width * 0.08f
        val verticalPadding = size.height * 0.06f
        val scale = min(
            (size.width - horizontalPadding * 2f) / projectedLongitudeSpan,
            (size.height - verticalPadding * 2f) / latitudeSpan,
        )
        val mapWidth = projectedLongitudeSpan * scale
        val mapHeight = latitudeSpan * scale
        val left = (size.width - mapWidth) / 2f
        val top = (
            (size.height - mapHeight) / 2f - size.height * 0.07f
        ).coerceAtLeast(verticalPadding)
        val outlineWidth = max(0.8f, size.minDimension * 0.0035f)

        fun project(point: GeoPoint): Offset = Offset(
            x = left + (point.longitude - bounds.minLongitude) * longitudeFactor * scale,
            y = top + (bounds.maxLatitude - point.latitude) * scale,
        )

        provinces.forEach { province ->
            val isVisited = province.code in visitedRegionCodes
            val fillColor = if (isVisited) Color(0xFF55D5A0) else Color(0xFF303B4D)
            val outlineColor = if (isVisited) Color(0xFF9AF0C5) else Color(0xFF7B879B)

            province.rings.forEach { ring ->
                if (ring.size < 3) return@forEach
                val path = Path().apply {
                    ring.forEachIndexed { index, point ->
                        val screen = project(point)
                        if (index == 0) moveTo(screen.x, screen.y) else lineTo(screen.x, screen.y)
                    }
                    close()
                }
                drawPath(path = path, color = fillColor)
                drawPath(
                    path = path,
                    color = outlineColor,
                    style = Stroke(width = outlineWidth),
                )
            }
        }
    }
}

private data class KoreaBounds(
    val minLongitude: Float,
    val maxLongitude: Float,
    val minLatitude: Float,
    val maxLatitude: Float,
) {
    companion object {
        fun from(provinces: List<ProvincePolygon>): KoreaBounds {
            val points = provinces.flatMap { province -> province.rings.flatten() }
            return KoreaBounds(
                minLongitude = points.minOf(GeoPoint::longitude),
                maxLongitude = points.maxOf(GeoPoint::longitude),
                minLatitude = points.minOf(GeoPoint::latitude),
                maxLatitude = points.maxOf(GeoPoint::latitude),
            )
        }
    }
}
