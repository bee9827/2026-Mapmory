package com.mapmory.shared.presentation.travelrecord

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TravelRecordDetailScreen(
    uiState: TravelRecordDetailUiState,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBackClick) {
            Text("목록으로")
        }

        when (uiState) {
            TravelRecordDetailUiState.Idle,
            TravelRecordDetailUiState.Loading,
            TravelRecordDetailUiState.Deleting,
            -> CircularProgressIndicator()

            is TravelRecordDetailUiState.Error -> Text(
                text = uiState.message,
                color = MaterialTheme.colorScheme.error,
            )

            is TravelRecordDetailUiState.Success -> {
                val record = uiState.record
                Text(record.title, style = MaterialTheme.typography.headlineMedium)
                Text(record.content, style = MaterialTheme.typography.bodyLarge)
                record.startDate?.let { startDate ->
                    Text(if (record.endDate == null) startDate else "$startDate ~ ${record.endDate}")
                }
                if (record.media.isNotEmpty()) {
                    Text("사진 ${record.media.size}장")
                }
                Text("작성일: ${record.createdAt}", style = MaterialTheme.typography.bodySmall)
                Text("수정일: ${record.updatedAt}", style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { showDeleteDialog = true }) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
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
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            },
        )
    }
}
