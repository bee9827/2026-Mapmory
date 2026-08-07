package com.mapmory.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MapmoryApp(
    isCameraPermissionGranted: Boolean? = null,
    galleryPermissionState: GalleryPermissionState? = null,
    onRequestCameraPermission: (() -> Unit)? = null,
    onRequestGalleryPermission: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Mapmory")

        if (onRequestCameraPermission != null) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onRequestCameraPermission,
                enabled = isCameraPermissionGranted != true,
            ) {
                Text(if (isCameraPermissionGranted == true) "카메라 허용됨" else "카메라 권한 요청")
            }
        }

        if (onRequestGalleryPermission != null) {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onRequestGalleryPermission,
                enabled = galleryPermissionState != GalleryPermissionState.FULL,
            ) {
                Text(galleryPermissionState.galleryButtonText())
            }
        }
    }
}

enum class GalleryPermissionState {
    FULL,
    PARTIAL,
    DENIED,
}

private fun GalleryPermissionState?.galleryButtonText(): String = when (this) {
    GalleryPermissionState.FULL -> "갤러리 허용됨"
    GalleryPermissionState.PARTIAL -> "사진 추가 선택"
    GalleryPermissionState.DENIED, null -> "갤러리 권한 요청"
}
