package com.mapmory.android.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

enum class GalleryAccess {
    FULL,
    PARTIAL,
    DENIED,
}

data class MediaPermissionRequesters(
    val requestCamera: () -> Unit,
    val requestGallery: () -> Unit,
)

@Composable
fun rememberMediaPermissionRequesters(
    onCameraPermissionResult: (Boolean) -> Unit,
    onGalleryPermissionResult: (GalleryAccess) -> Unit,
): MediaPermissionRequesters {
    val context = LocalContext.current

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onCameraPermissionResult,
    )
    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        onGalleryPermissionResult(context.currentGalleryAccess())
    }

    return remember(cameraPermissionLauncher, galleryPermissionLauncher) {
        MediaPermissionRequesters(
            requestCamera = {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
            requestGallery = {
                galleryPermissionLauncher.launch(requiredGalleryPermissions())
            },
        )
    }
}

fun Context.hasCameraPermission(): Boolean = hasPermission(Manifest.permission.CAMERA)

fun Context.currentGalleryAccess(): GalleryAccess = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        hasPermission(Manifest.permission.READ_MEDIA_IMAGES) -> GalleryAccess.FULL

    Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
        hasPermission(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) -> GalleryAccess.PARTIAL

    Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 &&
        hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE) -> GalleryAccess.FULL

    else -> GalleryAccess.DENIED
}

private fun requiredGalleryPermissions(): Array<String> = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
    )

    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
    )

    Build.VERSION.SDK_INT <= Build.VERSION_CODES.P -> arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )

    else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
}

private fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
