package com.mapmory.shared.presentation.map.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.mapmory.shared.presentation.map.data.GeneratedWorldMapData
import com.mapmory.shared.presentation.map.math.Quaternion
import com.mapmory.shared.presentation.map.math.Vec3
import com.mapmory.shared.presentation.map.math.clipToFrontHemisphere
import com.mapmory.shared.presentation.map.math.projectToScreen
import com.mapmory.shared.presentation.map.math.toSphere
import kotlin.math.PI
import kotlin.math.min

@Composable
internal fun WorldGlobe(
    visitedCountryCodes: Set<String>,
    modifier: Modifier = Modifier,
) {
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var longitude by remember { mutableStateOf(InitialLongitude) }
    var latitude by remember { mutableStateOf(0f) }
    var zoom by remember { mutableStateOf(1f) }

    // Keep the globe roll-free: longitude spin plus bounded latitude tilt only.
    val rotation = remember(longitude, latitude) {
        val polarRotation = Quaternion.fromAxisAngle(
            axis = Vec3(0f, 1f, 0f),
            angle = longitude,
        )
        val tiltRotation = Quaternion.fromAxisAngle(
            axis = Vec3(1f, 0f, 0f),
            angle = latitude,
        )
        (tiltRotation * polarRotation).normalized()
    }

    val countries = remember { GeneratedWorldMapData.countries }
    val baseRings = remember(countries) {
        countries.flatMap { country ->
            country.rings.map { ring ->
                country to ring.map { point -> point.toSphere() }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { viewportSize = it }
            .pointerInput(viewportSize) {
                detectTransformGestures(
                    panZoomLock = true,
                ) { _, pan, zoomChange, _ ->
                    val radius = min(
                        viewportSize.width.toFloat(),
                        viewportSize.height.toFloat(),
                    ) * 0.42f * zoom
                    if (radius <= 0f) return@detectTransformGestures

                    // Horizontal drag spins around the North–South polar axis.
                    longitude += pan.x / radius * RotationSensitivity
                    // Vertical drag only tilts within a predictable latitude range.
                    latitude = (latitude + pan.y / radius * RotationSensitivity)
                        .coerceIn(-MaxTilt, MaxTilt)

                    // zoomChange is > 1 for pinch-out and < 1 for pinch-in.
                    zoom = (zoom * zoomChange).coerceIn(MinZoom, MaxZoom)
                }
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val globeRadius = min(size.width, size.height) * 0.42f * zoom
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1B293A),
                        Color(0xFF111C2A),
                        Color(0xFF0B111C),
                    ),
                    center = center - Offset(globeRadius * 0.22f, globeRadius * 0.22f),
                    radius = globeRadius * 1.15f,
                ),
                radius = globeRadius,
                center = center,
            )

            baseRings.forEach { (country, points) ->
                val rotated = points.map(rotation::rotate)
                val clipped = clipToFrontHemisphere(rotated)
                if (clipped.size < 3) return@forEach

                val path = Path()
                clipped.forEachIndexed { index, point ->
                    val screen = projectToScreen(point, center, globeRadius)
                    if (index == 0) path.moveTo(screen.x, screen.y) else path.lineTo(screen.x, screen.y)
                }
                path.close()

                val isVisited = country.code in visitedCountryCodes
                drawPath(
                    path = path,
                    color = if (isVisited) Color(0xFF3FD09A) else Color(0xFF303B4D),
                )
                drawPath(
                    path = path,
                    color = if (isVisited) Color(0xFF8AEBC1) else Color(0xFF68758A),
                    style = Stroke(width = 1.1.dp.toPx()),
                )
            }

            drawCircle(
                color = Color(0xFF738199).copy(alpha = 0.58f),
                radius = globeRadius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx()),
            )
        }
    }
}

private val InitialLongitude = -127f * PI.toFloat() / 180f
private const val RotationSensitivity = 1.35f
private const val MaxTilt = 1.15f
private const val MinZoom = 0.75f
private const val MaxZoom = 4f
