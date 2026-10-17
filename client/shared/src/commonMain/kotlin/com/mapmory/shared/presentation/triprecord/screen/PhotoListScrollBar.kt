package com.mapmory.shared.presentation.triprecord.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

internal data class PhotoPickerScrollBarMetrics(
    val thumbFraction: Float,
    val scrollFraction: Float,
)

internal data class PhotoPickerScrollTarget(
    val itemIndex: Int,
    val itemScrollOffset: Int,
)

internal fun photoPickerScrollTarget(
    scrollFraction: Float,
    viewportSize: Int,
    itemSizes: List<Int>,
    itemSpacing: Int,
    contentPaddingBefore: Int,
    contentPaddingAfter: Int,
): PhotoPickerScrollTarget? {
    if (viewportSize <= 0 || itemSizes.isEmpty()) return null
    val contentSize = contentPaddingBefore + contentPaddingAfter +
        itemSizes.sum() + itemSpacing * (itemSizes.size - 1).coerceAtLeast(0)
    val maximumScrollOffset = (contentSize - viewportSize).coerceAtLeast(0)
    var remainingOffset =
        (scrollFraction.coerceIn(0f, 1f) * maximumScrollOffset).roundToInt() - contentPaddingBefore
    if (remainingOffset <= 0) return PhotoPickerScrollTarget(0, 0)

    itemSizes.forEachIndexed { index, itemSize ->
        if (remainingOffset < itemSize + itemSpacing || index == itemSizes.lastIndex) {
            return PhotoPickerScrollTarget(
                itemIndex = index,
                itemScrollOffset = remainingOffset.coerceIn(0, itemSize),
            )
        }
        remainingOffset -= itemSize + itemSpacing
    }
    return PhotoPickerScrollTarget(itemSizes.lastIndex, itemSizes.last())
}

internal fun photoPickerScrollBarMetrics(
    scrollOffset: Int,
    viewportSize: Int,
    contentSize: Int,
): PhotoPickerScrollBarMetrics? {
    if (viewportSize <= 0 || contentSize <= viewportSize) return null
    val maxScrollOffset = contentSize - viewportSize
    return PhotoPickerScrollBarMetrics(
        thumbFraction = (viewportSize.toFloat() / contentSize).coerceIn(0f, 1f),
        scrollFraction = (scrollOffset.toFloat() / maxScrollOffset).coerceIn(0f, 1f),
    )
}

@Composable
internal fun PhotoListScrollBar(
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier,
    layoutKey: Any = Unit,
) {
    val layoutInfo = listState.layoutInfo
    if (layoutInfo.visibleItemsInfo.isEmpty() || layoutInfo.totalItemsCount <= 0) return
    val itemSpacing = layoutInfo.mainAxisItemSpacing
    val contentPaddingBefore = layoutInfo.beforeContentPadding
    val contentPaddingAfter = layoutInfo.afterContentPadding
    val knownItemSizes = remember(listState, layoutKey, layoutInfo.totalItemsCount) { mutableMapOf<Int, Int>() }
    layoutInfo.visibleItemsInfo.forEach { item -> knownItemSizes[item.index] = item.size }
    val estimatedItemSize = knownItemSizes.values.average().roundToInt().coerceAtLeast(1)
    val estimatedContentSize = contentPaddingBefore + contentPaddingAfter +
        estimatedItemSize * layoutInfo.totalItemsCount +
        knownItemSizes.values.sumOf { it - estimatedItemSize } +
        itemSpacing * (layoutInfo.totalItemsCount - 1).coerceAtLeast(0)
    val estimatedScrollOffset =
        listState.firstVisibleItemIndex * (estimatedItemSize + itemSpacing) +
        knownItemSizes.entries.sumOf { (index, height) ->
            if (index < listState.firstVisibleItemIndex) height - estimatedItemSize else 0
        } + listState.firstVisibleItemScrollOffset
    val viewportSize = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
    val scrollOffset = when {
        !listState.canScrollBackward -> 0
        !listState.canScrollForward -> estimatedContentSize - viewportSize
        else -> estimatedScrollOffset
    }
    val metrics = photoPickerScrollBarMetrics(
        scrollOffset = scrollOffset,
        viewportSize = viewportSize,
        contentSize = estimatedContentSize,
    ) ?: return
    val trackColor = TripRecordPalette.current.line.copy(alpha = 0.65f)
    val thumbColor = TripRecordPalette.current.accent.copy(alpha = 0.9f)
    var scrollRequestVersion by remember { mutableStateOf(0) }
    var requestedScroll by remember { mutableStateOf<Pair<Int, Float>?>(null) }
    LaunchedEffect(requestedScroll) {
        val fraction = requestedScroll?.second ?: return@LaunchedEffect
        val target = photoPickerScrollTarget(
            scrollFraction = fraction,
            viewportSize = viewportSize,
            itemSizes = List(layoutInfo.totalItemsCount) { index ->
                knownItemSizes[index] ?: estimatedItemSize
            },
            itemSpacing = itemSpacing,
            contentPaddingBefore = contentPaddingBefore,
            contentPaddingAfter = contentPaddingAfter,
        ) ?: return@LaunchedEffect
        listState.scrollToItem(target.itemIndex, target.itemScrollOffset)
    }
    val latestRequest by rememberUpdatedState<(Float) -> Unit> { fraction ->
        scrollRequestVersion += 1
        requestedScroll = scrollRequestVersion to fraction
    }
    val latestMetrics by rememberUpdatedState(metrics)
    Canvas(
        modifier = modifier
            .width(32.dp)
            .fillMaxHeight()
            .testTag("photo-picker-scrollbar")
            .semantics { contentDescription = "사진 목록 스크롤바" }
            .pointerInput(listState) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    // Keep the grab point within the thumb stable for the whole drag.
                    val thumbHeight = (size.height * latestMetrics.thumbFraction)
                        .coerceAtLeast(44.dp.toPx()).coerceAtMost(size.height.toFloat())
                    val travel = (size.height - thumbHeight).coerceAtLeast(1f)
                    val thumbTop = travel * latestMetrics.scrollFraction
                    val grabOffset = if (down.position.y in thumbTop..(thumbTop + thumbHeight)) {
                        down.position.y - thumbTop
                    } else {
                        thumbHeight / 2f
                    }
                    fun request(y: Float) {
                        latestRequest(((y - grabOffset) / travel).coerceIn(0f, 1f))
                    }
                    request(down.position.y)
                    do {
                        val event = awaitPointerEvent()
                        val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
                        pointer.consume()
                        if (pointer.pressed) request(pointer.position.y)
                    } while (pointer.pressed)
                }
            },
    ) {
        val thumbHeight = (size.height * metrics.thumbFraction)
            .coerceAtLeast(44.dp.toPx())
            .coerceAtMost(size.height)
        val thumbOffset = (size.height - thumbHeight) * metrics.scrollFraction
        val barWidth = 4.dp.toPx()
        val barOffsetX = (size.width - barWidth) / 2f
        val cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f)
        drawRoundRect(
            color = trackColor,
            topLeft = Offset(barOffsetX, 0f),
            size = androidx.compose.ui.geometry.Size(barWidth, size.height),
            cornerRadius = cornerRadius,
        )
        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(barOffsetX, thumbOffset),
            size = androidx.compose.ui.geometry.Size(barWidth, thumbHeight),
            cornerRadius = cornerRadius,
        )
    }
}
