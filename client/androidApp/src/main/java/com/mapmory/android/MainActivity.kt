package com.mapmory.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.mapmory.android.permission.GalleryAccess
import com.mapmory.android.permission.currentGalleryAccess
import com.mapmory.android.permission.hasCameraPermission
import com.mapmory.android.permission.rememberMediaPermissionRequesters
import com.mapmory.shared.GalleryPermissionState
import com.mapmory.shared.MapmoryApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            var isCameraPermissionGranted by remember {
                mutableStateOf(context.hasCameraPermission())
            }
            var galleryAccess by remember {
                mutableStateOf(context.currentGalleryAccess())
            }
            val permissionRequesters = rememberMediaPermissionRequesters(
                onCameraPermissionResult = { isCameraPermissionGranted = it },
                onGalleryPermissionResult = { galleryAccess = it },
            )

            MapmoryApp(
                isCameraPermissionGranted = isCameraPermissionGranted,
                galleryPermissionState = galleryAccess.toPermissionState(),
                onRequestCameraPermission = permissionRequesters.requestCamera,
                onRequestGalleryPermission = permissionRequesters.requestGallery,
            )
        }
    }
}

private fun GalleryAccess.toPermissionState(): GalleryPermissionState = when (this) {
    GalleryAccess.FULL -> GalleryPermissionState.FULL
    GalleryAccess.PARTIAL -> GalleryPermissionState.PARTIAL
    GalleryAccess.DENIED -> GalleryPermissionState.DENIED
}
