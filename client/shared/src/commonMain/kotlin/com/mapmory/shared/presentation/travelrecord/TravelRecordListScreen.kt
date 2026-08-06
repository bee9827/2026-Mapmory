package com.mapmory.shared.presentation.travelrecord

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.TravelRecord
import com.mapmory.shared.domain.model.TravelRecordQuery

@Composable
fun TravelRecordListScreen(
    uiState: TravelRecordListUiState,
    query: TravelRecordQuery,
    locations: List<Location>,
    onKeywordChanged: (String) -> Unit,
    onLocationChanged: (Long?) -> Unit,
    onSearchClick: () -> Unit,
    onCreateClick: () -> Unit,
    onRecordClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "여행 기록",
            style = MaterialTheme.typography.headlineMedium,
        )
        Button(onClick = onCreateClick) {
            Text("기록 작성")
        }
        OutlinedTextField(
            value = query.keyword.orEmpty(),
            onValueChange = onKeywordChanged,
            label = { Text("제목·내용 검색") },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = { onLocationChanged(null) }) {
                Text(if (query.locationId == null) "✓ 전체" else "전체")
            }
            locations.forEach { location ->
                OutlinedButton(onClick = { onLocationChanged(location.id) }) {
                    Text(if (query.locationId == location.id) "✓ ${location.name}" else location.name)
                }
            }
        }
        Button(onClick = onSearchClick) {
            Text("검색")
        }

        when (uiState) {
            TravelRecordListUiState.Idle,
            TravelRecordListUiState.Loading,
            -> CircularProgressIndicator()

            is TravelRecordListUiState.Error -> Text(uiState.message)

            is TravelRecordListUiState.Success -> {
                if (uiState.records.isEmpty()) {
                    Text(
                        if (query.keyword == null && query.locationId == null) {
                            "아직 작성한 여행 기록이 없어요."
                        } else {
                            "조건에 맞는 여행 기록이 없어요."
                        },
                    )
                } else {
                    TravelRecordList(
                        records = uiState.records,
                        onRecordClick = onRecordClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun TravelRecordList(
    records: List<TravelRecord>,
    onRecordClick: (Long) -> Unit,
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(records, key = TravelRecord::id) { record ->
            Card(
                onClick = { onRecordClick(record.id) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(record.title, style = MaterialTheme.typography.titleMedium)
                    Text(record.content, style = MaterialTheme.typography.bodyMedium)
                    record.startDate?.let { startDate ->
                        Text(
                            text = if (record.endDate == null) startDate else "$startDate ~ ${record.endDate}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}
