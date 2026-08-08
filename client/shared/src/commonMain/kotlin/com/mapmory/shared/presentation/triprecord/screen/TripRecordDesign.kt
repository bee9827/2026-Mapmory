package com.mapmory.shared.presentation.triprecord.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal object TripRecordPalette {
    val background = Color(0xFF07171B)
    val surface = Color(0xFF0C2026)
    val surfaceElevated = Color(0xFF102A32)
    val line = Color(0xFF1B363E)
    val text = Color(0xFFE9F4F2)
    val muted = Color(0xFF81999E)
    val accent = Color(0xFF19E5A2)
    val accentSoft = Color(0xFF123E3A)
    val danger = Color(0xFFFF6264)
}

@Composable
internal fun TripRecordTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = TripRecordPalette.accent,
            onPrimary = TripRecordPalette.background,
            secondary = TripRecordPalette.muted,
            background = TripRecordPalette.background,
            onBackground = TripRecordPalette.text,
            surface = TripRecordPalette.surface,
            onSurface = TripRecordPalette.text,
            surfaceVariant = TripRecordPalette.surfaceElevated,
            onSurfaceVariant = TripRecordPalette.muted,
            outline = TripRecordPalette.line,
            error = TripRecordPalette.danger,
        ),
        content = content,
    )
}

@Composable
internal fun TripRecordBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    TripRecordTheme {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = TripRecordPalette.background,
            content = content,
        )
    }
}

@Composable
internal fun TripRecordTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBackClick != null) {
            TripIconButton(
                label = "←",
                onClick = onBackClick,
            )
            Spacer(Modifier.width(14.dp))
        } else {
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = title,
            color = TripRecordPalette.text,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.weight(1f))
        trailing?.invoke()
    }
}

@Composable
internal fun TripIconButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(TripRecordPalette.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = TripRecordPalette.text,
            fontSize = if (label == "•••") 20.sp else 28.sp,
            fontWeight = FontWeight.Light,
        )
    }
}

@Composable
internal fun TripSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = TripRecordPalette.muted,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier,
    )
}

@Composable
internal fun TripBottomBar(
    selected: TripBottomTab,
    onRecordClick: () -> Unit = {},
    onMapClick: () -> Unit = {},
    onCreateClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(TripRecordPalette.background)
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TripBottomItem(
            tab = TripBottomTab.MAP,
            selected = selected == TripBottomTab.MAP,
            onClick = onMapClick,
        )
        TripBottomItem(
            tab = TripBottomTab.RECORD,
            selected = selected == TripBottomTab.RECORD,
            onClick = onRecordClick,
        )
        TripBottomItem(
            tab = TripBottomTab.CREATE,
            selected = selected == TripBottomTab.CREATE,
            onClick = onCreateClick,
        )
        TripBottomItem(
            tab = TripBottomTab.PROFILE,
            selected = selected == TripBottomTab.PROFILE,
            onClick = onProfileClick,
        )
    }
}

internal enum class TripBottomTab(
    val icon: String,
    val label: String,
) {
    MAP("⌖", "지도"),
    RECORD("▤", "기록"),
    CREATE("＋", "작성"),
    PROFILE("●", "내 정보"),
}

@Composable
private fun TripBottomItem(
    tab: TripBottomTab,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(58.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = tab.icon,
            color = if (selected) TripRecordPalette.accent else TripRecordPalette.muted,
            fontSize = if (tab == TripBottomTab.CREATE) 26.sp else 20.sp,
            lineHeight = 22.sp,
        )
        Text(
            text = tab.label,
            color = if (selected) TripRecordPalette.accent else TripRecordPalette.muted,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
internal fun TripPhotoPlaceholder(
    modifier: Modifier = Modifier,
    variant: Int = 0,
) {
    val skyColors = when (variant % 3) {
        0 -> listOf(Color(0xFFEEA16C), Color(0xFFE56A66), Color(0xFF305C6B))
        1 -> listOf(Color(0xFFB5C991), Color(0xFF5A896F), Color(0xFF253E4A))
        else -> listOf(Color(0xFFB88F85), Color(0xFF596D8E), Color(0xFF203846))
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient(skyColors)),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val sun = Offset(size.width * (0.72f - variant.coerceAtMost(2) * 0.12f), size.height * 0.28f)
            drawCircle(
                color = Color(0xFFFFE8AB),
                radius = size.minDimension * 0.09f,
                center = sun,
            )

            val backHill = Path().apply {
                moveTo(0f, size.height * 0.68f)
                lineTo(size.width * 0.2f, size.height * 0.47f)
                lineTo(size.width * 0.37f, size.height * 0.65f)
                lineTo(size.width * 0.58f, size.height * 0.42f)
                lineTo(size.width, size.height * 0.67f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(backHill, color = Color(0xFF456A62))

            val frontHill = Path().apply {
                moveTo(0f, size.height * 0.82f)
                lineTo(size.width * 0.32f, size.height * 0.64f)
                lineTo(size.width * 0.53f, size.height * 0.76f)
                lineTo(size.width * 0.77f, size.height * 0.55f)
                lineTo(size.width, size.height * 0.72f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(frontHill, color = Color(0xFF1B3C43))
            drawLine(
                color = Color.White.copy(alpha = 0.28f),
                start = Offset(size.width * 0.08f, size.height * 0.8f),
                end = Offset(size.width * 0.88f, size.height * 0.73f),
                strokeWidth = size.minDimension * 0.012f,
            )
        }
    }
}

@Composable
fun TripMapArtwork(
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(TripRecordPalette.background),
    ) {
        val scaleX = size.width / 320f
        val scaleY = size.height / 500f
        fun point(x: Float, y: Float) = Offset(x * scaleX, y * scaleY)

        val peninsula = Path().apply {
            moveTo(point(188f, 42f))
            lineTo(point(218f, 58f))
            lineTo(point(224f, 91f))
            lineTo(point(246f, 119f))
            lineTo(point(235f, 147f))
            lineTo(point(252f, 179f))
            lineTo(point(237f, 213f))
            lineTo(point(221f, 236f))
            lineTo(point(226f, 274f))
            lineTo(point(205f, 296f))
            lineTo(point(190f, 329f))
            lineTo(point(164f, 338f))
            lineTo(point(143f, 319f))
            lineTo(point(119f, 325f))
            lineTo(point(105f, 300f))
            lineTo(point(82f, 288f))
            lineTo(point(66f, 259f))
            lineTo(point(73f, 225f))
            lineTo(point(89f, 201f))
            lineTo(point(95f, 164f))
            lineTo(point(119f, 145f))
            lineTo(point(135f, 113f))
            lineTo(point(161f, 96f))
            lineTo(point(164f, 64f))
            close()
        }
        drawPath(peninsula, color = Color(0xFF112B32), style = Fill)
        drawPath(
            peninsula,
            color = Color(0xFF254049),
            style = Stroke(width = size.minDimension * 0.008f),
        )

        val highlightedSouth = Path().apply {
            moveTo(point(75f, 228f))
            lineTo(point(91f, 208f))
            lineTo(point(110f, 217f))
            lineTo(point(126f, 203f))
            lineTo(point(145f, 220f))
            lineTo(point(161f, 246f))
            lineTo(point(153f, 274f))
            lineTo(point(133f, 285f))
            lineTo(point(112f, 274f))
            lineTo(point(93f, 282f))
            lineTo(point(79f, 263f))
            close()
        }
        drawPath(highlightedSouth, color = Color(0xFFB8F2D1), style = Fill)
        drawPath(
            highlightedSouth,
            color = Color(0xFF9FE8BF),
            style = Stroke(width = size.minDimension * 0.006f),
        )

        val provinceLines = listOf(
            listOf(164f to 64f, 170f to 116f, 154f to 160f, 145f to 220f),
            listOf(119f to 145f, 153f to 160f, 191f to 151f, 235f to 147f),
            listOf(89f to 201f, 126f to 203f, 161f to 193f, 221f to 182f),
            listOf(66f to 259f, 112f to 274f, 153f to 274f, 190f to 246f),
            listOf(135f to 113f, 145f to 160f, 126f to 203f, 133f to 285f),
        )
        provinceLines.forEach { line ->
            val path = Path().apply {
                line.forEachIndexed { index, (x, y) ->
                    if (index == 0) moveTo(point(x, y)) else lineTo(point(x, y))
                }
            }
            drawPath(
                path,
                color = Color(0xFF1C353D),
                style = Stroke(width = size.minDimension * 0.004f),
            )
        }

        drawOval(
            color = Color(0xFF74E7B2),
            topLeft = point(109f, 360f),
            size = androidx.compose.ui.geometry.Size(38f * scaleX, 15f * scaleY),
        )
        drawOval(
            color = Color(0xFF1C353D),
            topLeft = point(80f, 184f),
            size = androidx.compose.ui.geometry.Size(7f * scaleX, 4f * scaleY),
        )
    }
}

private fun Path.moveTo(point: Offset) {
    moveTo(point.x, point.y)
}

private fun Path.lineTo(point: Offset) {
    lineTo(point.x, point.y)
}
