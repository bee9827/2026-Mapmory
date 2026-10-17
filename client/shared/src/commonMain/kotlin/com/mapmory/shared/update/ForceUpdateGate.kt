package com.mapmory.shared.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/** A modal gate keeps the underlying navigation/input state intact on foreground checks. */
@Composable
internal fun ForceUpdateGate(state: ForceUpdateState, onRetry: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    var storeError by remember { mutableStateOf(false) }
    if (!state.isBlocking) {
        if (state.isStoreUpdateAvailable) {
            AlertDialog(
                onDismissRequest = state::dismissStoreUpdate,
                title = { Text("새 버전이 있어요") },
                text = {
                    Column {
                        Text("최신 버전으로 업데이트하고 새로운 기능을 만나보세요.")
                        if (storeError) Text("스토어를 열지 못했어요. 다시 시도해 주세요.")
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        storeError = runCatching { uriHandler.openUri(state.storeUrl) }.isFailure
                    }) { Text("업데이트") }
                },
                dismissButton = {
                    TextButton(onClick = state::dismissStoreUpdate) { Text("나중에") }
                },
            )
        }
        return
    }
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.safeDrawingPadding().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            ) {
                if (state.isUpdateRequired) {
                    Text("업데이트가 필요해요", style = MaterialTheme.typography.headlineSmall)
                    Text("맵모리를 계속 이용하려면 최신 버전으로 업데이트해 주세요.")
                    Text("현재 ${state.currentVersion} · 최소 ${state.minimumVersion}")
                    Button(onClick = {
                        storeError = runCatching { uriHandler.openUri(state.storeUrl) }.isFailure
                    }) { Text("업데이트") }
                    if (storeError) Text("스토어를 열지 못했어요. 다시 시도해 주세요.")
                    TextButton(onClick = onRetry) { Text("다시 확인") }
                } else {
                    CircularProgressIndicator()
                    Text("앱 버전을 확인하고 있어요")
                }
            }
        }
    }
}
