package com.mapmory.shared.presentation.triprecord.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.LocationType
import com.mapmory.shared.domain.model.TripRecordData
import com.mapmory.shared.domain.model.TripRecordQuery
import com.mapmory.shared.presentation.triprecord.state.TripRecordListUiState

@Composable
fun TripRecordListScreen(
    uiState: TripRecordListUiState,
    query: TripRecordQuery,
    locations: List<Location>,
    onKeywordChanged: (String) -> Unit,
    onLocationChanged: (Long?) -> Unit,
    onSearchClick: () -> Unit,
    onPreviousPageClick: () -> Unit,
    onNextPageClick: () -> Unit,
    onCreateClick: () -> Unit,
    onMapClick: () -> Unit,
    onRecordClick: (Long) -> Unit,
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    TripRecordBackground(modifier = modifier) {
        Column(Modifier.fillMaxSize()) {
            TripRecordTopBar(
                title = "Mapmory",
                trailing = {
                    Text(
                        text = "나의 여행 기록",
                        color = TripRecordPalette.muted,
                        fontSize = 11.sp,
                    )
                },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "나의 여행기록",
                    color = TripRecordPalette.text,
                    fontSize = 29.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "여행의 순간을 다시 꺼내보세요",
                    color = TripRecordPalette.muted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 7.dp),
                )
                Spacer(Modifier.height(20.dp))
                LocationFilters(
                    query = query,
                    locations = locations,
                    onLocationChanged = onLocationChanged,
                    onSearchClick = onSearchClick,
                )
                Spacer(Modifier.height(16.dp))

                when (uiState) {
                    TripRecordListUiState.Idle,
                    TripRecordListUiState.Loading,
                    -> CircularProgressIndicator(
                        color = TripRecordPalette.accent,
                        modifier = Modifier.padding(top = 20.dp),
                    )

                    is TripRecordListUiState.Error -> Text(
                        text = uiState.message,
                        color = TripRecordPalette.danger,
                        modifier = Modifier.padding(top = 20.dp),
                    )

                    is TripRecordListUiState.Success -> {
                        if (uiState.records.isEmpty()) {
                            EmptyTripRecords(
                                hasFilter = query.keyword != null || query.locationId != null,
                                modifier = Modifier.weight(1f),
                            )
                        } else {
                            TripRecordList(
                                records = uiState.records,
                                locations = locations,
                                onRecordClick = onRecordClick,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (uiState.totalPages > 1) {
                            PageControls(
                                page = uiState.page,
                                totalPages = uiState.totalPages,
                                onPreviousPageClick = onPreviousPageClick,
                                onNextPageClick = onNextPageClick,
                            )
                        }
                    }
                }
            }

            TripBottomBar(
                selected = TripBottomTab.RECORD,
                onMapClick = onMapClick,
                onCreateClick = onCreateClick,
                onProfileClick = onProfileClick,
            )
        }
    }
}

@Composable
private fun LocationFilters(
    query: TripRecordQuery,
    locations: List<Location>,
    onLocationChanged: (Long?) -> Unit,
    onSearchClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TripFilterChip(
            text = "전체",
            selected = query.locationId == null,
            onClick = {
                onLocationChanged(null)
                onSearchClick()
            },
        )
        locations
            .filter { it.type == LocationType.DISTRICT }
            .take(5)
            .forEach { location ->
                TripFilterChip(
                    text = location.name,
                    selected = query.locationId == location.id,
                    onClick = {
                        onLocationChanged(location.id)
                        onSearchClick()
                    },
                )
            }
    }
}

@Composable
private fun TripFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        color = if (selected) TripRecordPalette.background else TripRecordPalette.muted,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clickable(onClick = onClick)
            .then(
                Modifier
                    .padding(horizontal = 14.dp, vertical = 9.dp)
                    .background(
                        color = if (selected) TripRecordPalette.accent else TripRecordPalette.surface,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                    ),
            )
            .padding(horizontal = 2.dp),
    )
}

@Composable
private fun TripRecordList(
    records: List<TripRecordData>,
    locations: List<Location>,
    onRecordClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 18.dp),
    ) {
        items(records, key = TripRecordData::id) { record ->
            TripRecordCard(
                record = record,
                location = locations.firstOrNull { it.id == record.locationId },
                onClick = { onRecordClick(record.id) },
            )
        }
    }
}

@Composable
private fun TripRecordCard(
    record: TripRecordData,
    location: Location?,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = TripRecordPalette.surface),
    ) {
        Column {
            TripPhotoPlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(222.dp),
                variant = record.id.toInt(),
            )
            Column(Modifier.padding(18.dp)) {
                Text(
                    text = record.title,
                    color = TripRecordPalette.text,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = record.content,
                    color = TripRecordPalette.muted,
                    fontSize = 13.sp,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 7.dp),
                )
                Column(
                    modifier = Modifier.padding(top = 15.dp),
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
            }
        }
    }
}

@Composable
private fun EmptyTripRecords(
    hasFilter: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (hasFilter) "조건에 맞는 여행 기록이 없어요." else "아직 작성한 여행 기록이 없어요.",
            color = TripRecordPalette.text,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "새로운 여행의 순간을 기록해 보세요.",
            color = TripRecordPalette.muted,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun PageControls(
    page: Int,
    totalPages: Int,
    onPreviousPageClick: () -> Unit,
    onNextPageClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "‹ 이전",
            color = if (page > 0) TripRecordPalette.accent else TripRecordPalette.muted.copy(alpha = 0.45f),
            modifier = Modifier.clickable(enabled = page > 0, onClick = onPreviousPageClick),
        )
        Text(
            text = "${page + 1} / $totalPages",
            color = Color.White,
            fontSize = 12.sp,
        )
        Text(
            text = "다음 ›",
            color = if (page + 1 < totalPages) TripRecordPalette.accent else TripRecordPalette.muted.copy(alpha = 0.45f),
            modifier = Modifier.clickable(
                enabled = page + 1 < totalPages,
                onClick = onNextPageClick,
            ),
        )
    }
}
