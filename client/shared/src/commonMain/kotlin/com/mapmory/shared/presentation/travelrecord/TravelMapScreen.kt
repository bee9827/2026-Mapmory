package com.mapmory.shared.presentation.travelrecord

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TravelMapScreen(
    mapContent: @Composable () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        mapContent()
        TextButton(
            onClick = onBackClick,
            modifier = Modifier.padding(16.dp),
        ) {
            Text("목록으로")
        }
    }
}
