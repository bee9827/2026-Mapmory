package com.mapmory.shared.presentation.travelrecord

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mapmory.shared.domain.model.Location

@Composable
fun TravelRecordEditorScreen(
    uiState: TravelRecordEditorUiState,
    locations: List<Location>,
    onLocationSelected: (Location) -> Unit,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit,
    onStartDateChanged: (String) -> Unit,
    onEndDateChanged: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBackClick) {
            Text("목록으로")
        }
        Text("여행 기록 작성", style = MaterialTheme.typography.headlineMedium)
        Text("장소 선택", style = MaterialTheme.typography.titleMedium)
        locations.forEach { location ->
            OutlinedButton(
                onClick = { onLocationSelected(location) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (uiState.selectedLocation?.id == location.id) "✓ ${location.name}" else location.name,
                )
            }
        }
        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChanged,
            label = { Text("제목") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = uiState.content,
            onValueChange = onContentChanged,
            label = { Text("내용") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = uiState.startDate,
            onValueChange = onStartDateChanged,
            label = { Text("시작일 (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = uiState.endDate,
            onValueChange = onEndDateChanged,
            label = { Text("종료일 (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
        )
        uiState.errorMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = onSaveClick,
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (uiState.isSaving) "저장 중..." else "저장")
        }
    }
}
