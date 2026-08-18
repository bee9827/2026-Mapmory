package com.mapmory.shared.presentation.photo

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Address
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import com.mapmory.shared.data.local.photo.PhotoMetadataDatabase
import com.mapmory.shared.data.local.photo.PhotoMetadataEntity
import com.mapmory.shared.domain.model.Location
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberPhotoLibraryActions(
    onPhotosPicked: (List<SelectedPhoto>) -> Unit,
    onPhotosRecommended: (List<SelectedPhoto>) -> Unit,
    onMessage: (String) -> Unit,
): PhotoLibraryActions {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val latestPicked by rememberUpdatedState(onPhotosPicked)
    val latestRecommended by rememberUpdatedState(onPhotosRecommended)
    val latestMessage by rememberUpdatedState(onMessage)
    var pendingRecommendation by remember { mutableStateOf<Pair<Location, String?>?>(null) }

    fun loadRecommendations(target: Location, parentName: String?) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { context.recommendPhotos(target, parentName) }
            }
            result.onSuccess(latestRecommended).onFailure {
                latestMessage("사진 추천을 불러오지 못했어요. 잠시 후 다시 시도해 주세요.")
            }
        }
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        val canRead = context.canRecommendPhotos()
        val target = pendingRecommendation
        pendingRecommendation = null
        if (canRead && target != null) {
            loadRecommendations(target.first, target.second)
        } else {
            latestMessage("장소 기반 추천을 사용하려면 사진 접근을 허용해 주세요.")
        }
    }

    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(MaxPhotosPerRecord),
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        scope.launch {
            val photos = withContext(Dispatchers.IO) {
                uris.mapNotNull { uri -> context.readPhoto(uri) }
            }
            if (photos.isEmpty()) {
                latestMessage("선택한 사진을 읽지 못했어요.")
            } else {
                latestPicked(photos)
            }
        }
    }

    return remember(context, galleryPicker, galleryPermissionLauncher) {
        PhotoLibraryActions(
            pickFromGallery = {
                galleryPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
            recommendForLocation = { location, parentName ->
                if (context.canRecommendPhotos()) {
                    loadRecommendations(location, parentName)
                } else {
                    pendingRecommendation = location to parentName
                    galleryPermissionLauncher.launch(requiredRecommendationPermissions())
                }
            },
        )
    }
}

private fun Context.canReadGallery(): Boolean {
    val permissions = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
        )
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> listOf(Manifest.permission.READ_MEDIA_IMAGES)
        else -> listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    return permissions.any { permission ->
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
}

private fun Context.canRecommendPhotos(): Boolean =
    canReadGallery() && (
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_MEDIA_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        )

private fun requiredRecommendationPermissions(): Array<String> = buildList {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.READ_MEDIA_IMAGES)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        }
    } else {
        add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        add(Manifest.permission.ACCESS_MEDIA_LOCATION)
    }
}.toTypedArray()

private data class GalleryEntry(
    val photo: PhotoMetadataEntity,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Float,
)

@Suppress("DEPRECATION")
private suspend fun Context.recommendPhotos(
    target: Location,
    parentName: String?,
): List<SelectedPhoto> {
    val geocoder = Geocoder(this, Locale.KOREA)
    val targetAddress = geocoder
        .getFromLocationName(target.recommendationSearchText(parentName), 1)
        ?.firstOrNull()
        ?: return emptyList()
    val targetLatitude = targetAddress.latitude
    val targetLongitude = targetAddress.longitude
    val radius = target.recommendationRadiusMeters()
    val photos = syncPhotoMetadata()
    val entries = photos.mapNotNull { photo ->
        val latitude = photo.latitude ?: return@mapNotNull null
        val longitude = photo.longitude ?: return@mapNotNull null
        val distance = FloatArray(1)
        android.location.Location.distanceBetween(
            targetLatitude,
            targetLongitude,
            latitude,
            longitude,
            distance,
        )
        if (distance[0] > radius) return@mapNotNull null
        GalleryEntry(photo, latitude, longitude, distance[0])
    }

    if (!Geocoder.isPresent()) {
        error("이 기기에서는 사진 위치의 행정구역을 확인할 수 없어요.")
    }

    return entries
        .sortedBy(GalleryEntry::distanceMeters)
        .take(MaxReverseGeocodeCandidates)
        .filter { entry ->
            runCatching {
                geocoder
                    .getFromLocation(entry.latitude, entry.longitude, 1)
                    ?.firstOrNull()
                    ?.toAdministrativeArea()
                    ?.matches(target, parentName) == true
            }.getOrDefault(false)
        }
        .take(MaxRecommendedPhotos)
        .mapNotNull { entry ->
            readPhoto(
                uri = Uri.parse(entry.photo.contentUri),
                knownName = entry.photo.displayName,
                knownCoordinates = entry.latitude to entry.longitude,
                knownCapturedAtMillis = entry.photo.capturedAtMillis,
            )
        }
}

private suspend fun Context.syncPhotoMetadata(): List<PhotoMetadataEntity> {
    val dao = PhotoMetadataDatabase.getInstance(this).photoMetadataDao()
    val previousById = dao.getAll().associateBy(PhotoMetadataEntity::mediaId)
    val scanId = System.currentTimeMillis()
    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATE_TAKEN,
        MediaStore.Images.Media.DATE_MODIFIED,
        MediaStore.Images.Media.MIME_TYPE,
        MediaStore.Images.Media.SIZE,
        MediaStore.Images.Media.WIDTH,
        MediaStore.Images.Media.HEIGHT,
    )
    val photos = contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        "${MediaStore.Images.Media.DATE_TAKEN} DESC",
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        buildList<PhotoMetadataEntity> {
            while (cursor.moveToNext()) {
                val mediaId = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    mediaId,
                )
                val modifiedAtSeconds = cursor.getLongOrNull(MediaStore.Images.Media.DATE_MODIFIED) ?: 0L
                val previous = previousById[mediaId]
                val coordinates = if (
                    previous != null &&
                    previous.modifiedAtSeconds == modifiedAtSeconds &&
                    previous.latitude != null &&
                    previous.longitude != null
                ) {
                    requireNotNull(previous.latitude) to requireNotNull(previous.longitude)
                } else {
                    readCoordinates(uri)
                }
                add(
                    PhotoMetadataEntity(
                        mediaId = mediaId,
                        contentUri = uri.toString(),
                        displayName = cursor.getStringOrNull(MediaStore.Images.Media.DISPLAY_NAME)
                            ?: "여행 사진",
                        capturedAtMillis = cursor.getLongOrNull(MediaStore.Images.Media.DATE_TAKEN)
                            ?.takeIf { it > 0L },
                        modifiedAtSeconds = modifiedAtSeconds,
                        latitude = coordinates?.first,
                        longitude = coordinates?.second,
                        mimeType = cursor.getStringOrNull(MediaStore.Images.Media.MIME_TYPE),
                        sizeBytes = cursor.getLongOrNull(MediaStore.Images.Media.SIZE) ?: 0L,
                        width = cursor.getIntOrNull(MediaStore.Images.Media.WIDTH) ?: 0,
                        height = cursor.getIntOrNull(MediaStore.Images.Media.HEIGHT) ?: 0,
                        scanId = scanId,
                    ),
                )
            }
        }
    } ?: return emptyList()

    dao.replaceSnapshot(photos, scanId)
    return photos
}

private fun Address.toAdministrativeArea(): PhotoAdministrativeArea = PhotoAdministrativeArea(
    countryCode = countryCode,
    administrativeArea = adminArea,
    subAdministrativeArea = subAdminArea,
    locality = locality,
    subLocality = subLocality,
)

private fun Context.readPhoto(
    uri: Uri,
    knownName: String? = null,
    knownCoordinates: Pair<Double, Double>? = null,
    knownCapturedAtMillis: Long? = null,
): SelectedPhoto? = runCatching {
    val metadata = if (knownName == null && knownCapturedAtMillis == null) {
        queryPhotoMetadata(uri)
    } else {
        null to null
    }
    val coordinates = knownCoordinates ?: readCoordinates(uri)
    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        contentResolver.loadThumbnail(uri, Size(PreviewSizePx, PreviewSizePx), null)
    } else {
        contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
    } ?: return null
    val bytes = ByteArrayOutputStream().use { output ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, PreviewJpegQuality, output)
        output.toByteArray()
    }
    SelectedPhoto(
        id = uri.toString(),
        displayName = knownName ?: metadata.first ?: "여행 사진",
        previewBytes = bytes,
        latitude = coordinates?.first,
        longitude = coordinates?.second,
        capturedAt = formatDate(knownCapturedAtMillis ?: metadata.second),
    )
}.getOrNull()

private fun Context.queryPhotoMetadata(uri: Uri): Pair<String?, Long?> {
    val projection = arrayOf(
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATE_TAKEN,
    )
    return contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        if (!cursor.moveToFirst()) return@use null to null
        val name = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            .takeIf { it >= 0 }
            ?.let(cursor::getString)
        val date = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
            .takeIf { it >= 0 }
            ?.let(cursor::getLong)
            ?.takeIf { it > 0L }
        name to date
    } ?: (null to null)
}

private fun Context.readCoordinates(uri: Uri): Pair<Double, Double>? = runCatching {
    val metadataUri = if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_MEDIA_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        MediaStore.setRequireOriginal(uri)
    } else {
        uri
    }
    contentResolver.openInputStream(metadataUri)?.use { input ->
        ExifInterface(input).latLong?.let { it[0] to it[1] }
    }
}.getOrNull()

private fun Cursor.getStringOrNull(columnName: String): String? =
    getColumnIndex(columnName).takeIf { it >= 0 }?.let(::getString)

private fun Cursor.getLongOrNull(columnName: String): Long? =
    getColumnIndex(columnName).takeIf { it >= 0 && !isNull(it) }?.let(::getLong)

private fun Cursor.getIntOrNull(columnName: String): Int? =
    getColumnIndex(columnName).takeIf { it >= 0 && !isNull(it) }?.let(::getInt)

private fun formatDate(epochMillis: Long?): String? = epochMillis?.let {
    SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date(it))
}

private const val PreviewSizePx = 960
private const val PreviewJpegQuality = 84
private const val MaxReverseGeocodeCandidates = 80
private const val MaxRecommendedPhotos = 12
