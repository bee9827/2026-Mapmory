package com.mapmory.shared.presentation.triprecord.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapmory.shared.LocalMapmoryTheme
import com.mapmory.shared.presentation.map.ui.KoreaMapArtwork
import com.mapmory.shared.preview.PreviewSurface
import org.jetbrains.compose.resources.decodeToImageBitmap

@Immutable
internal data class TripRecordColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val line: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val accentSoft: Color,
    val danger: Color,
    val photoRecommendText: Color,
    val photoRecommendBackground: Color,
    val photoRecommendBorder: Color,
    val photoGalleryBackground: Color,
    val photoGalleryBorder: Color,
    val pageBackground: Color,
    val softSurface: Color,
    val border: Color,
    val primary: Color,
    val primarySoft: Color,
    val secondaryAccent: Color,
    val onPrimary: Color,
    val headingText: Color,
    val bodyText: Color,
    val secondaryText: Color,
    val navigationDivider: Color,
    val navigationUnselected: Color,
    val navigationSelectedLabel: Color,
    val contentOnMedia: Color,
    val mediaScrim: Color,
    val metadataDateBackground: Color,
)

@Immutable
internal data class TripMapColors(
    val logoText: Color,
    val scopeBackground: Color,
    val scopeBorder: Color,
    val scopeSelectedBackground: Color,
    val scopeSelectedText: Color,
    val scopeUnselectedText: Color,
    val tagBackground: Color,
    val tagText: Color,
    val tagSelectedText: Color,
    val dashboardBadgeText: Color,
)

@Immutable
internal data class TripStatisticsColors(
    val divider: Color,
    val mapBorder: Color,
    val mapLand: Color,
    val mapOutline: Color,
)

private val LightTripRecordColors = TripRecordColors(
    background = Color(0xFFFAFCFB), surface = Color.White, surfaceElevated = Color(0xFFF7FAF8),
    line = Color(0xFFE1E7E3), text = Color(0xFF1F2924), muted = Color(0xFF89948E),
    accent = Color(0xFF4D9272), accentSoft = Color(0xFFE9F2ED), danger = Color(0xFFC94C57),
    photoRecommendText = Color(0xFFBB4D56), photoRecommendBackground = Color(0xFFFFF1F1),
    photoRecommendBorder = Color(0xFFDB6A70), photoGalleryBackground = Color(0xFFF3F7F4),
    photoGalleryBorder = Color(0xFFD9E6DE), pageBackground = Color(0xFFFAFCFB),
    softSurface = Color(0xFFF0F4F1), border = Color(0xFFE4E9E6), primary = Color(0xFF4D9272),
    primarySoft = Color(0xFFE9F2ED), secondaryAccent = Color(0xFF4A896B), onPrimary = Color.White,
    headingText = Color(0xFF1F2924), bodyText = Color(0xFF5F6E66), secondaryText = Color(0xFF89948E),
    navigationDivider = Color(0xFFE1E7E3), navigationUnselected = Color(0xFF9AA59F),
    navigationSelectedLabel = Color(0xFF5F6E66), contentOnMedia = Color.White,
    mediaScrim = Color.Black.copy(alpha = 0.62f), metadataDateBackground = Color(0xFFF0F4F1),
)

private val DarkTripRecordColors = TripRecordColors(
    background = Color(0xFF111518), surface = Color(0xFF1A1E22), surfaceElevated = Color(0xFF102A32),
    line = Color(0xFF1B363E), text = Color(0xFFE9F4F2), muted = Color(0xFF81999E),
    accent = Color(0xFF35C988), accentSoft = Color(0xFF123E3A), danger = Color(0xFFFF6264),
    photoRecommendText = Color.White, photoRecommendBackground = Color(0xFF382125),
    photoRecommendBorder = Color(0xFF99555D), photoGalleryBackground = Color(0xFF1B2D26),
    photoGalleryBorder = Color(0xFF3E7960), pageBackground = Color(0xFF121518),
    softSurface = Color(0xFF1C2124), border = Color(0xFF2B3135), primary = Color(0xFF35C987),
    primarySoft = Color(0xFF173B2D), secondaryAccent = Color(0xFF67D9A2), onPrimary = Color(0xFF071B12),
    headingText = Color(0xFFF1F5F3), bodyText = Color(0xFFBDC6C2), secondaryText = Color(0xFF89938F),
    navigationDivider = Color(0xFF2C3431), navigationUnselected = Color(0xFF77827D),
    navigationSelectedLabel = Color(0xFFA2ADA7), contentOnMedia = Color.White,
    mediaScrim = Color.Black.copy(alpha = 0.62f), metadataDateBackground = Color(0xFF24292D),
)

private val LightTripMapColors = TripMapColors(
    logoText = Color(0xFF1F2924), scopeBackground = Color(0xFFF1F5F2), scopeBorder = Color(0xFFDCE7E0),
    scopeSelectedBackground = Color.White, scopeSelectedText = Color(0xFF2D4539),
    scopeUnselectedText = Color(0xFF7A8880), tagBackground = Color(0xFFF7FAF8),
    tagText = Color(0xFF6B786F), tagSelectedText = Color.White, dashboardBadgeText = Color(0xFF4A896B),
)

private val DarkTripMapColors = TripMapColors(
    logoText = Color(0xFFF4F8F5), scopeBackground = Color(0xFF151C19), scopeBorder = Color(0xFF2D3A34),
    scopeSelectedBackground = Color(0xFF2A3832), scopeSelectedText = Color(0xFFEEF7F1),
    scopeUnselectedText = Color(0xFF92A09A), tagBackground = Color(0xFF1A2421),
    tagText = Color(0xFFBDC8C2), tagSelectedText = Color(0xFF072118), dashboardBadgeText = Color(0xFF9CE6BF),
)

private val LightTripStatisticsColors = TripStatisticsColors(
    divider = Color(0xFFE6EBE8), mapBorder = Color(0xFFE1E7E3),
    mapLand = Color(0xFFDCE4DF), mapOutline = Color(0xFFC8D3CC),
)

private val DarkTripStatisticsColors = TripStatisticsColors(
    divider = Color(0xFF30363A), mapBorder = Color(0xFF343B40),
    mapLand = Color(0xFF293039), mapOutline = Color(0xFF424B53),
)

internal val TripRecordPalette = staticCompositionLocalOf { LightTripRecordColors }
internal val TripMapPalette = staticCompositionLocalOf { LightTripMapColors }
internal val TripStatisticsPalette = staticCompositionLocalOf { LightTripStatisticsColors }

@Composable
internal fun ProvideTripRecordPalettes(
    isDark: Boolean,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        TripRecordPalette provides if (isDark) DarkTripRecordColors else LightTripRecordColors,
        TripMapPalette provides if (isDark) DarkTripMapColors else LightTripMapColors,
        TripStatisticsPalette provides if (isDark) DarkTripStatisticsColors else LightTripStatisticsColors,
        content = content,
    )
}

@Composable
internal fun rememberDismissKeyboardOnTapModifier(): Modifier {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    return Modifier.pointerInput(focusManager, keyboardController) {
        detectTapGestures {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
        }
    }
}

@Composable
internal fun TripRecordTheme(content: @Composable () -> Unit) {
    val palette = TripRecordPalette.current
    val colors = if (LocalMapmoryTheme.current.isDark) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = palette.background,
            secondary = palette.muted,
            background = palette.background,
            onBackground = palette.text,
            surface = palette.surface,
            onSurface = palette.text,
            surfaceVariant = palette.surfaceElevated,
            onSurfaceVariant = palette.muted,
            outline = palette.line,
            error = palette.danger,
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = Color.White,
            secondary = palette.muted,
            background = palette.background,
            onBackground = palette.text,
            surface = palette.surface,
            onSurface = palette.text,
            surfaceVariant = palette.surfaceElevated,
            onSurfaceVariant = palette.muted,
            outline = palette.line,
            error = palette.danger,
        )
    }
    MaterialTheme(
        colorScheme = colors,
        content = content,
    )
}

@Composable
internal fun TripRecordBackground(
    modifier: Modifier = Modifier,
    backgroundColor: Color = TripRecordPalette.current.background,
    content: @Composable () -> Unit,
) {
    TripRecordTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = backgroundColor,
        ) {
            Box(
                modifier = modifier.fillMaxSize(),
            ) {
                content()
            }
        }
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
                contentDescription = "뒤로가기",
                onClick = onBackClick,
            )
            Spacer(Modifier.width(14.dp))
        } else {
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = title,
            color = TripRecordPalette.current.text,
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
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = TripRecordPalette.current.surface,
    contentColor: Color = TripRecordPalette.current.text,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(containerColor)
            .semantics { this.contentDescription = contentDescription }
            .clickable(
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = contentColor,
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
        color = TripRecordPalette.current.muted,
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
    backgroundColor: Color = TripRecordPalette.current.background,
    dividerColor: Color = TripRecordPalette.current.line,
    selectedIconColor: Color = TripRecordPalette.current.accent,
    selectedLabelColor: Color = TripRecordPalette.current.accent,
    unselectedColor: Color = TripRecordPalette.current.muted,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(backgroundColor)
            .drawBehind {
                drawLine(
                    color = dividerColor,
                    start = Offset.Zero,
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                )
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TripBottomItem(
            tab = TripBottomTab.MAP,
            selected = selected == TripBottomTab.MAP,
            onClick = onMapClick,
            selectedIconColor = selectedIconColor,
            selectedLabelColor = selectedLabelColor,
            unselectedColor = unselectedColor,
            modifier = Modifier.weight(1f),
        )
        TripBottomItem(
            tab = TripBottomTab.RECORD,
            selected = selected == TripBottomTab.RECORD,
            onClick = onRecordClick,
            selectedIconColor = selectedIconColor,
            selectedLabelColor = selectedLabelColor,
            unselectedColor = unselectedColor,
            modifier = Modifier.weight(1f),
        )
        TripBottomItem(
            tab = TripBottomTab.PROFILE,
            selected = selected == TripBottomTab.PROFILE,
            onClick = onProfileClick,
            selectedIconColor = selectedIconColor,
            selectedLabelColor = selectedLabelColor,
            unselectedColor = unselectedColor,
            modifier = Modifier.weight(1f),
        )
    }
}

internal enum class TripBottomTab(val label: String) {
    MAP("지도"),
    RECORD("일지"),
    PROFILE("통계"),
}

@Composable
private fun TripBottomItem(
    tab: TripBottomTab,
    selected: Boolean,
    onClick: () -> Unit,
    selectedIconColor: Color,
    selectedLabelColor: Color,
    unselectedColor: Color,
    modifier: Modifier = Modifier,
) {
    val iconColor = if (selected) selectedIconColor else unselectedColor
    val labelColor = if (selected) selectedLabelColor else unselectedColor
    Column(
        modifier = modifier
            .height(64.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        TripBottomIcon(
            tab = tab,
            color = iconColor,
            modifier = Modifier.size(25.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = tab.label,
            color = labelColor,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
        )
    }
}

@Composable
private fun TripBottomIcon(
    tab: TripBottomTab,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        val stroke = Stroke(
            width = 1.7.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        when (tab) {
            TripBottomTab.MAP -> {
                val mapPath = Path().apply {
                    moveTo(size.width * 0.12f, size.height * 0.23f)
                    lineTo(size.width * 0.36f, size.height * 0.10f)
                    lineTo(size.width * 0.66f, size.height * 0.23f)
                    lineTo(size.width * 0.90f, size.height * 0.10f)
                    lineTo(size.width * 0.90f, size.height * 0.77f)
                    lineTo(size.width * 0.66f, size.height * 0.90f)
                    lineTo(size.width * 0.36f, size.height * 0.77f)
                    lineTo(size.width * 0.12f, size.height * 0.90f)
                    close()
                }
                drawPath(mapPath, color = color, style = stroke)
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.36f, size.height * 0.10f),
                    end = Offset(size.width * 0.36f, size.height * 0.77f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.66f, size.height * 0.23f),
                    end = Offset(size.width * 0.66f, size.height * 0.90f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round,
                )
            }

            TripBottomTab.RECORD -> {
                listOf(0.18f, 0.47f, 0.76f).forEach { y ->
                    drawRect(
                        color = color,
                        topLeft = Offset(size.width * 0.12f, size.height * y),
                        size = Size(size.width * 0.17f, size.height * 0.17f),
                        style = stroke,
                    )
                    drawLine(
                        color = color,
                        start = Offset(size.width * 0.43f, size.height * (y + 0.085f)),
                        end = Offset(size.width * 0.90f, size.height * (y + 0.085f)),
                        strokeWidth = stroke.width,
                        cap = StrokeCap.Round,
                    )
                }
            }

            TripBottomTab.PROFILE -> {
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.08f, size.height * 0.88f),
                    end = Offset(size.width * 0.92f, size.height * 0.88f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round,
                )
                listOf(
                    0.24f to 0.50f,
                    0.50f to 0.16f,
                    0.76f to 0.34f,
                ).forEach { (x, top) ->
                    drawLine(
                        color = color,
                        start = Offset(size.width * x, size.height * top),
                        end = Offset(size.width * x, size.height * 0.88f),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }
        }
    }
}

@Composable
internal fun TripPhotoPlaceholder(
    modifier: Modifier = Modifier,
    variant: Int = 0,
    shape: Shape = RoundedCornerShape(18.dp),
) {
    val skyColors = when (variant % 3) {
        0 -> listOf(Color(0xFFEEA16C), Color(0xFFE56A66), Color(0xFF305C6B))
        1 -> listOf(Color(0xFFB5C991), Color(0xFF5A896F), Color(0xFF253E4A))
        else -> listOf(Color(0xFFB88F85), Color(0xFF596D8E), Color(0xFF203846))
    }
    Box(
        modifier = modifier
            .clip(shape)
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
internal fun TripPhotoImage(
    imageBytes: ByteArray?,
    fallbackBytes: ByteArray? = null,
    contentDescription: String,
    modifier: Modifier = Modifier,
    placeholderVariant: Int = 0,
    shape: Shape = RoundedCornerShape(18.dp),
) {
    val bitmap = remember(imageBytes, fallbackBytes) {
        imageBytes.decodeToImageBitmapOrNull()
            ?: fallbackBytes.decodeToImageBitmapOrNull()
    }
    if (bitmap == null) {
        TripPhotoPlaceholder(modifier, placeholderVariant, shape)
    } else {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(shape),
        )
    }
}

private fun ByteArray?.decodeToImageBitmapOrNull() =
    this?.let { bytes -> runCatching { bytes.decodeToImageBitmap() }.getOrNull() }

@Composable
fun TripMapArtwork(
    modifier: Modifier = Modifier,
) {
    KoreaMapArtwork(modifier = modifier)
}

@Preview(
    name = "여행 지도 아트워크",
    showBackground = true,
    widthDp = 412,
    heightDp = 500,
)
@Composable
fun TripMapArtworkPreview() {
    PreviewSurface { TripMapArtwork() }
}
