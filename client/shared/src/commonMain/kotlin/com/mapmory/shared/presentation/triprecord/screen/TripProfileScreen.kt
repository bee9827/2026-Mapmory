package com.mapmory.shared.presentation.triprecord.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapmory.shared.PrivacyPolicy
import com.mapmory.shared.presentation.map.data.GeneratedWorldMapData
import com.mapmory.shared.presentation.triprecord.state.TopLocationUiModel
import com.mapmory.shared.presentation.triprecord.state.TripStatisticsUiModel
import com.mapmory.shared.presentation.triprecord.state.TripStatisticsUiState
import kotlin.math.abs

@Composable
fun TripProfileScreen(
    onMapClick: () -> Unit,
    onRecordClick: () -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit,
    statisticsUiState: TripStatisticsUiState = TripStatisticsUiState.Success(TripStatisticsUiModel.Empty),
    modifier: Modifier = Modifier,
) {
    var showSettings by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    TripRecordBackground(modifier = modifier, backgroundColor = StatsBackground) {
        Column(Modifier.fillMaxSize().background(StatsBackground)) {
            StatisticsHeader(onSettingsClick = { showSettings = true })

            when (statisticsUiState) {
                TripStatisticsUiState.Loading -> Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = StatsPrimary)
                }

                is TripStatisticsUiState.Error -> Box(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(statisticsUiState.message, color = StatsMuted, fontSize = 13.sp)
                }

                is TripStatisticsUiState.Success -> StatisticsContent(
                    statistics = statisticsUiState.statistics,
                    modifier = Modifier.weight(1f),
                )
            }

            TripBottomBar(
                selected = TripBottomTab.PROFILE,
                onMapClick = onMapClick,
                onRecordClick = onRecordClick,
                onCreateClick = onCreateClick,
                onProfileClick = onProfileClick,
                backgroundColor = StatsBackground,
                dividerColor = StatsNavigationDivider,
                selectedIconColor = StatsPrimary,
                selectedLabelColor = StatsNavigationSelectedLabel,
                unselectedColor = StatsNavigationUnselected,
                contentTopPadding = 6.dp,
            )
        }
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            containerColor = StatsCardBackground,
            title = { Text("설정", color = StatsText) },
            text = { Text("Mapmory의 서비스 정책을 확인할 수 있어요.", color = StatsBody) },
            confirmButton = {
                if (PrivacyPolicy.URL.isNotBlank()) {
                    TextButton(onClick = { uriHandler.openUri(PrivacyPolicy.URL) }) {
                        Text("개인정보 처리방침", color = StatsPrimary)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettings = false }) {
                    Text("닫기", color = StatsMuted)
                }
            },
        )
    }
}

@Composable
private fun StatisticsHeader(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "MY TRAVEL DATA",
                color = StatsPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp,
            )
            Text(
                text = "여행 통계",
                color = StatsText,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Text(
            text = "⚙",
            color = StatsMuted,
            fontSize = 21.sp,
            modifier = Modifier.clickable(onClick = onSettingsClick).padding(7.dp),
        )
    }
}

@Composable
private fun StatisticsContent(
    statistics: TripStatisticsUiModel,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        item { PassportCard(statistics) }
        item { VisitProgressCard(statistics) }
        item { RankingCard(statistics.topLocations) }
    }
}

@Composable
private fun PassportCard(statistics: TripStatisticsUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, StatsLine),
        colors = CardDefaults.cardColors(containerColor = StatsCardBackground),
    ) {
        Column(Modifier.padding(start = 14.dp, top = 18.dp, end = 14.dp, bottom = 13.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "MAPMORY PASSPORT",
                    color = StatsAccent,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp,
                )
                Text(
                    text = "${statistics.travelerName}의 방문 지도",
                    color = StatsText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(10.dp))
            PassportWorldMap(
                visitedCountryCodes = statistics.visitedCountryCodes,
                modifier = Modifier.fillMaxWidth().aspectRatio(2f),
            )
            Row(
                modifier = Modifier.padding(start = 4.dp, top = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(10.dp).background(StatsPrimary, RoundedCornerShape(3.dp)))
                Spacer(Modifier.width(7.dp))
                Text(
                    text = "색칠된 국가는 여행 일지가 있는 곳이에요",
                    color = StatsMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun PassportWorldMap(
    visitedCountryCodes: Set<String>,
    modifier: Modifier = Modifier,
) {
    val countries = remember { GeneratedWorldMapData.countries }
    val mapShape = RoundedCornerShape(15.dp)
    Canvas(
        modifier = modifier
            .clip(mapShape)
            .background(StatsBackground)
            .border(1.dp, StatsMapBorder, mapShape),
    ) {
        countries.filterNot { it.code == "AQ" }.forEach { country ->
            country.rings.forEach { ring ->
                if (ring.size < 3) return@forEach

                val segments = mutableListOf<MutableList<com.mapmory.shared.presentation.map.domain.GeoPoint>>()
                ring.forEach { point ->
                    val current = segments.lastOrNull()
                    val previous = current?.lastOrNull()
                    if (previous != null && abs(point.longitude - previous.longitude) > 180f) {
                        segments.add(mutableListOf(point))
                    } else if (current == null) {
                        segments.add(mutableListOf(point))
                    } else {
                        current += point
                    }
                }

                segments.filter { it.size >= 3 }.forEach { segment ->
                    val path = Path().apply {
                        segment.forEachIndexed { index, point ->
                            val x = (point.longitude + 180f) / 360f * size.width
                            val y = (90f - point.latitude) / 180f * size.height
                            if (index == 0) moveTo(x, y) else lineTo(x, y)
                        }
                        close()
                    }
                    val visited = country.code in visitedCountryCodes
                    drawPath(path, color = if (visited) StatsPrimary else StatsMapLand)
                    drawPath(
                        path,
                        color = if (visited) StatsAccent.copy(alpha = 0.75f) else StatsMapOutline,
                        style = Stroke(width = 0.7.dp.toPx()),
                    )
                }
            }
        }
    }
}

@Composable
private fun VisitProgressCard(statistics: TripStatisticsUiModel) {
    StatsCard {
        VisitProgress("전세계 방문률", statistics.worldVisitedCount, 195, "개국")
        Spacer(Modifier.height(21.dp))
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(StatsDivider))
        Spacer(Modifier.height(18.dp))
        VisitProgress("대한민국 방문률", statistics.koreaVisitedCount, 17, "지역")
        Spacer(Modifier.height(20.dp))
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(StatsDivider))
        Row(modifier = Modifier.fillMaxWidth().padding(top = 17.dp)) {
            SummaryValue("여행 기록", statistics.recordCount, Modifier.weight(1f))
            SummaryValue("사진", statistics.photoCount, Modifier.weight(1f))
        }
    }
}

@Composable
private fun VisitProgress(label: String, value: Int, total: Int, unit: String) {
    Text(label, color = StatsBody, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    Row(
        modifier = Modifier.padding(top = 9.dp, bottom = 12.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(value.toString(), color = StatsText, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(unit, color = StatsAccent, fontSize = 13.sp, modifier = Modifier.padding(start = 4.dp, bottom = 3.dp))
    }
    ProgressBar(progress = value.toFloat() / total)
}

@Composable
private fun ProgressBar(progress: Float) {
    Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(StatsSoft, CircleShape)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(10.dp)
                .background(StatsPrimary, CircleShape),
        )
    }
}

@Composable
private fun SummaryValue(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, color = StatsMuted, fontSize = 11.sp)
        Text(
            text = value.toString(),
            color = StatsText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun RankingCard(topLocations: List<TopLocationUiModel>) {
    StatsCard {
        Text("가장 많이 방문한 곳", color = StatsText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        if (topLocations.isEmpty()) {
            Text(
                text = "아직 집계할 여행 기록이 없어요",
                color = StatsMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 17.dp, bottom = 4.dp),
            )
        } else {
            Column(
                modifier = Modifier.padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                topLocations.take(3).forEachIndexed { index, location ->
                    RankingRow(index, location)
                }
            }
        }
    }
}

@Composable
private fun RankingRow(index: Int, location: TopLocationUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(26.dp).background(
                color = if (index == 0) StatsPrimary else StatsSoft,
                shape = CircleShape,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = (index + 1).toString(),
                color = if (index == 0) StatsOnPrimary else StatsMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = location.locationName,
            color = StatsText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 10.dp).weight(1f),
        )
        Text(
            text = "${location.visitCount}회",
            color = StatsAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun StatsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, StatsLine),
        colors = CardDefaults.cardColors(containerColor = StatsCardBackground),
    ) {
        Column(Modifier.padding(21.dp)) { content() }
    }
}

private val StatsBackground = Color(0xFF121518)
private val StatsCardBackground = Color(0xFF1A1E22)
private val StatsLine = Color(0xFF2B3135)
private val StatsDivider = Color(0xFF30363A)
private val StatsSoft = Color(0xFF1C2124)
private val StatsPrimary = Color(0xFF35C987)
private val StatsAccent = Color(0xFF67D9A2)
private val StatsOnPrimary = Color(0xFF071B12)
private val StatsText = Color(0xFFF1F5F3)
private val StatsBody = Color(0xFFBDC6C2)
private val StatsMuted = Color(0xFF89938F)
private val StatsMapBorder = Color(0xFF343B40)
private val StatsMapLand = Color(0xFF293039)
private val StatsMapOutline = Color(0xFF424B53)
private val StatsNavigationDivider = Color(0xFF2C3431)
private val StatsNavigationUnselected = Color(0xFF77827D)
private val StatsNavigationSelectedLabel = Color(0xFFA2ADA7)
