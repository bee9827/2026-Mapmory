package com.mapmory.shared.presentation.triprecord.screen

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.LocationType
import com.mapmory.shared.presentation.triprecord.state.TripRecordEditorUiState

@Composable
fun TripRecordEditorScreen(
    uiState: TripRecordEditorUiState,
    locations: List<Location>,
    onProvinceChanged: () -> Unit,
    onLocationSelected: (Location) -> Unit,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit,
    onStartDateChanged: (String) -> Unit,
    onEndDateChanged: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val provinces = locations.filter { it.type == LocationType.PROVINCE }
    var selectedProvinceId by remember {
        mutableStateOf(uiState.selectedLocation?.parentId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBackClick) {
            Text("목록으로")
        }
        Text(
            text = if (uiState.recordId == null) "여행 기록 작성" else "여행 기록 수정",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text("시·도 선택", style = MaterialTheme.typography.titleMedium)
        provinces.forEach { province ->
            OutlinedButton(
                onClick = {
                    if (selectedProvinceId != province.id) onProvinceChanged()
                    selectedProvinceId = province.id
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (selectedProvinceId == province.id) "✓ ${province.name}" else province.name,
                )
            }
        }
        selectedProvinceId?.let { provinceId ->
            Text("시·군·구 선택", style = MaterialTheme.typography.titleMedium)
            locations
                .filter { it.type == LocationType.DISTRICT && it.parentId == provinceId }
                .forEach { district ->
                    OutlinedButton(
                        onClick = { onLocationSelected(district) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            if (uiState.selectedLocation?.id == district.id) {
                                "✓ ${district.name}"
                            } else {
                                district.name
                            },
                        )
                    }
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
            Text(
                when {
                    uiState.isSaving -> "저장 중..."
                    uiState.recordId == null -> "저장"
                    else -> "수정"
                },
            )
        }
    }
}
