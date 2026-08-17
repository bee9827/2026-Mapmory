package com.mapmory.shared.presentation.triprecord.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.TripRecordData
import com.mapmory.shared.presentation.triprecord.state.TripRecordDetailUiState

@Composable
fun TripRecordDetailScreen(
    uiState: TripRecordDetailUiState,
    locations: List<Location>,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMapClick: () -> Unit = {},
    onRecordClick: () -> Unit = {},
    onCreateClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    TripRecordBackground(modifier = modifier) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                when (uiState) {
                    TripRecordDetailUiState.Idle,
                    TripRecordDetailUiState.Loading,
                    TripRecordDetailUiState.Deleting,
                    -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TripRecordPalette.accent)
                    }

                    is TripRecordDetailUiState.Error -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(uiState.message, color = TripRecordPalette.danger)
                        TextButton(onClick = onBackClick) { Text("목록으로") }
                    }

                    is TripRecordDetailUiState.Success -> {
                        val record = uiState.record
                        val location = locations.firstOrNull { it.id == record.locationId }
                        TripRecordDetailContent(
                            record = record,
                            location = location,
                            onBackClick = onBackClick,
                            onEditClick = onEditClick,
                            onDeleteClick = { showDeleteDialog = true },
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("여행 기록 삭제") },
            text = { Text("삭제한 기록은 복구할 수 없습니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    },
                ) {
                    Text("삭제", color = TripRecordPalette.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("취소") }
            },
        )
    }
}

@Composable
private fun TripRecordDetailContent(
    record: TripRecordData,
    location: Location?,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TripRecordPhotoSection(
            record = record,
            onBackClick = onBackClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
        TripRecordBottomCard(
            record = record,
            location = location,
            modifier = Modifier
                .overlapPhoto(24.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.4f),
        )
    }
}

@Composable
private fun TripRecordPhotoSection(
    record: TripRecordData,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val media = remember(record.id, record.media) {
        record.media.sortedBy { it.sortOrder }
    }

    Box(modifier = modifier) {
        if (media.isEmpty()) {
            TripPhotoImage(
                previewBytes = null,
                contentDescription = record.title,
                modifier = Modifier.fillMaxSize(),
                placeholderVariant = record.id.toInt() + 1,
            )
        } else {
            val pagerState = rememberPagerState(pageCount = { media.size })
            LaunchedEffect(record.id) {
                pagerState.scrollToPage(0)
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val photo = media[page]
                TripPhotoImage(
                    previewBytes = photo.previewBytes,
                    contentDescription = "${record.title} 사진 ${page + 1}",
                    modifier = Modifier.fillMaxSize(),
                    placeholderVariant = record.id.toInt() + page + 1,
                )
            }
            if (media.size > 1) {
                PhotoPageIndicator(
                    pageCount = media.size,
                    currentPage = pagerState.currentPage,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 18.dp),
                )
            }
        }

        DetailTopActions(
            onBackClick = onBackClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun PhotoPageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .background(
                color = TripRecordPalette.background.copy(alpha = 0.72f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 9.dp, vertical = 7.dp),
    ) {
        repeat(pageCount) { page ->
            Box(
                modifier = Modifier
                    .size(if (page == currentPage) 7.dp else 5.dp)
                    .background(
                        color = if (page == currentPage) {
                            TripRecordPalette.accent
                        } else {
                            TripRecordPalette.muted.copy(alpha = 0.7f)
                        },
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Composable
private fun DetailTopActions(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        DetailBackButton(onClick = onBackClick)
        DetailMoreButton(
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
        )
    }
}

@Composable
private fun DetailBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TripIconButton(
        label = "←",
        onClick = onClick,
        containerColor = TripRecordPalette.surface.copy(alpha = 0.5f),
        contentColor = Color.White,
        modifier = modifier,
    )
}

@Composable
private fun DetailMoreButton(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        TripIconButton(
            label = "•••",
            onClick = { expanded = true },
            containerColor = TripRecordPalette.surface.copy(alpha = 0.5f),
            contentColor = Color.White,
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("수정") },
                onClick = {
                    expanded = false
                    onEditClick()
                },
            )
            DropdownMenuItem(
                text = { Text("삭제", color = TripRecordPalette.danger) },
                onClick = {
                    expanded = false
                    onDeleteClick()
                },
            )
        }
    }
}

@Composable
private fun TripRecordBottomCard(
    record: TripRecordData,
    location: Location?,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(record.id) {
        scrollState.scrollTo(0)
    }

    Column(
        modifier = modifier
            .background(
                color = TripRecordPalette.surface,
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            )
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        Column(
            modifier = Modifier.padding(bottom = 17.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "대한민국 · ${location?.name ?: "여행지"}",
                color = TripRecordPalette.accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
            record.startDate?.let { date ->
                Text(
                    text = date.replace('-', '.'),
                    color = TripRecordPalette.muted,
                    fontSize = 11.sp,
                )
            }
        }
        Text(
            text = record.title,
            color = TripRecordPalette.text,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = record.content,
            color = TripRecordPalette.muted,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

private fun Modifier.overlapPhoto(overlap: Dp): Modifier = layout { measurable, constraints ->
    val overlapPx = overlap.roundToPx()
    val placeable = measurable.measure(constraints)
    layout(
        width = placeable.width,
        height = (placeable.height - overlapPx).coerceAtLeast(0),
    ) {
        placeable.placeRelative(x = 0, y = -overlapPx)
    }
}
