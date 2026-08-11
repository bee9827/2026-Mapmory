package com.mapmory.shared.presentation.triprecord.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TripProfileScreen(
    onMapClick: () -> Unit,
    onRecordClick: () -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TripRecordBackground(modifier = modifier) {
        Column(Modifier.fillMaxSize()) {
            TripRecordTopBar(
                title = "Mapmory",
                trailing = {
                    Text(
                        text = "내 정보",
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
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "내 정보",
                    color = TripRecordPalette.text,
                    fontSize = 29.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "나의 여행 기록을 한눈에 관리해 보세요",
                    color = TripRecordPalette.muted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 7.dp),
                )
                Spacer(Modifier.height(24.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = TripRecordPalette.surface,
                            shape = RoundedCornerShape(20.dp),
                        )
                        .padding(20.dp),
                ) {
                    Text(
                        text = "여행자",
                        color = TripRecordPalette.text,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "나의 여행을 기록하는 중",
                        color = TripRecordPalette.muted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                    ) {
                        ProfileStat(label = "여행 기록", value = "0")
                        ProfileStat(label = "방문 지역", value = "0")
                    }
                }
            }

            TripBottomBar(
                selected = TripBottomTab.PROFILE,
                onMapClick = onMapClick,
                onRecordClick = onRecordClick,
                onCreateClick = onCreateClick,
                onProfileClick = onProfileClick,
            )
        }
    }
}

@Composable
private fun ProfileStat(
    label: String,
    value: String,
) {
    Column {
        Text(
            text = value,
            color = TripRecordPalette.accent,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            color = TripRecordPalette.muted,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}
