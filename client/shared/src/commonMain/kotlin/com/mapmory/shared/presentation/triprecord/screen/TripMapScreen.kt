package com.mapmory.shared.presentation.triprecord.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TripMapScreen(
    mapContent: @Composable () -> Unit,
    onBackClick: () -> Unit,
    onRecordClick: () -> Unit = onBackClick,
    onCreateClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    TripRecordBackground(modifier = modifier) {
        Box(Modifier.fillMaxSize()) {
            mapContent()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
            ) {
                TripRecordTopBar(
                    title = "Mapmory",
                    trailing = {
                        Text(
                            text = "나의 여행으로 채우는 지도",
                            color = TripRecordPalette.muted,
                            fontSize = 10.sp,
                        )
                    },
                )
                MapSummaryCard()
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 18.dp, bottom = 92.dp)
                    .size(66.dp)
                    .clip(CircleShape)
                    .background(TripRecordPalette.accent)
                    .clickable(onClick = onCreateClick),
                contentAlignment = Alignment.Center,
            ) {
                Text("＋", color = TripRecordPalette.background, fontSize = 39.sp, fontWeight = FontWeight.Light)
            }

            TripBottomBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                selected = TripBottomTab.MAP,
                onRecordClick = onRecordClick,
                onCreateClick = onCreateClick,
                onProfileClick = onProfileClick,
            )
        }
    }
}
@Composable
private fun MapSummaryCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = TripRecordPalette.background.copy(alpha = 0.96f),
                shape = RoundedCornerShape(18.dp),
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("나의 대한민국 지도", color = TripRecordPalette.muted, fontSize = 11.sp)
                Row(
                    modifier = Modifier.padding(top = 3.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text("2", color = TripRecordPalette.accent, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    Text(" / 17", color = TripRecordPalette.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                text = "1% 채움",
                color = TripRecordPalette.accent,
                fontSize = 11.sp,
                modifier = Modifier
                    .background(TripRecordPalette.accentSoft, RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
            )
        }
    }
}
