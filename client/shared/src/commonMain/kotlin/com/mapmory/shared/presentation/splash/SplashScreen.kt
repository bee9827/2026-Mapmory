package com.mapmory.shared.presentation.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapmory.shared.presentation.triprecord.screen.TripMapPalette
import com.mapmory.shared.presentation.triprecord.screen.TripRecordPalette

@Composable
internal fun MapmorySplashScreen(
    contentWindowInsets: WindowInsets,
    modifier: Modifier = Modifier,
) {
    val recordPalette = TripRecordPalette.current
    val mapPalette = TripMapPalette.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(recordPalette.pageBackground),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .windowInsetsPadding(contentWindowInsets)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            SplashMark(
                backgroundColor = recordPalette.primarySoft,
                borderColor = recordPalette.secondaryAccent.copy(alpha = 0.44f),
                iconColor = recordPalette.primary,
            )
            Spacer(Modifier.size(16.dp))
            Text(
                text = buildMapmoryWordmark(
                    mapColor = mapPalette.logoText,
                    moryColor = recordPalette.secondaryAccent,
                ),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-2.4).sp,
            )
            Spacer(Modifier.size(10.dp))
            Text(
                text = "여행의 순간을, 지도 위에",
                color = recordPalette.bodyText,
                fontSize = 14.sp,
                lineHeight = 22.sp,
            )
        }

        Text(
            text = "MAP YOUR MEMORIES",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(contentWindowInsets)
                .padding(bottom = 24.dp),
            color = recordPalette.secondaryText,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.6.sp,
        )
    }
}

@Composable
private fun SplashMark(
    backgroundColor: Color,
    borderColor: Color,
    iconColor: Color,
) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, CircleShape)
            .semantics { contentDescription = "Mapmory 로고" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(56.dp)) {
            val scale = size.minDimension / 48f
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                color = iconColor,
                center = center,
                radius = 17f * scale,
                style = Stroke(width = 1.8f * scale),
            )

            val route = Path().apply {
                moveTo(14f * scale, 28f * scale)
                cubicTo(
                    19f * scale,
                    21f * scale,
                    23f * scale,
                    32f * scale,
                    28f * scale,
                    26f * scale,
                )
                cubicTo(
                    32f * scale,
                    21f * scale,
                    32f * scale,
                    16f * scale,
                    38f * scale,
                    19f * scale,
                )
            }
            drawPath(
                path = route,
                color = iconColor,
                style = Stroke(
                    width = 2f * scale,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(1f * scale, 5f * scale),
                        phase = 0f,
                    ),
                ),
            )
            drawCircle(
                color = iconColor,
                center = Offset(14f * scale, 28f * scale),
                radius = 3f * scale,
            )
            drawCircle(
                color = iconColor,
                center = Offset(38f * scale, 19f * scale),
                radius = 3f * scale,
            )
        }
    }
}

private fun buildMapmoryWordmark(
    mapColor: Color,
    moryColor: Color,
): AnnotatedString = buildAnnotatedString {
    withStyle(SpanStyle(color = mapColor)) { append("Map") }
    withStyle(SpanStyle(color = moryColor)) { append("mory") }
}
