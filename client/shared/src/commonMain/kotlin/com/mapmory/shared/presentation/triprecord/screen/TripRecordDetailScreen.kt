package com.mapmory.shared.presentation.triprecord.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapmory.shared.domain.model.Location
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
                        Column(Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            ) {
                                TripPhotoImage(
                                    previewBytes = record.media.minByOrNull { it.sortOrder }?.previewBytes,
                                    contentDescription = record.title,
                                    modifier = Modifier.fillMaxSize(),
                                    placeholderVariant = record.id.toInt() + 1,
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 18.dp, vertical = 20.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    TripIconButton(label = "←", onClick = onBackClick)
                                    TripIconButton(label = "•••", onClick = {})
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = TripRecordPalette.surface,
                                        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                                    )
                                    .padding(horizontal = 24.dp, vertical = 24.dp),
                            ) {
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
                                Column(
                                    modifier = Modifier.padding(top = 17.dp),
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
                                Spacer(Modifier.height(21.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = if (record.media.isEmpty()) "여행 기록" else "사진 ${record.media.size}장",
                                        color = TripRecordPalette.muted,
                                        fontSize = 12.sp,
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(onClick = onEditClick) { Text("수정") }
                                        TextButton(onClick = { showDeleteDialog = true }) {
                                            Text("삭제", color = TripRecordPalette.danger)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            TripBottomBar(
                selected = TripBottomTab.RECORD,
                onMapClick = onMapClick,
                onRecordClick = onRecordClick,
                onCreateClick = onCreateClick,
                onProfileClick = onProfileClick,
            )
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
