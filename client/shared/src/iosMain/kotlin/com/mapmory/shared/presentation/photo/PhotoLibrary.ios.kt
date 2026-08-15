@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.mapmory.shared.presentation.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mapmory.shared.domain.model.Location
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGSizeMake
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLPlacemark
import platform.CoreLocation.CLLocation
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.getBytes
import platform.Foundation.NSSortDescriptor
import platform.Photos.PHAccessLevelReadWrite
import platform.Photos.PHAsset
import platform.Photos.PHAssetMediaTypeImage
import platform.Photos.PHAssetResource
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHFetchOptions
import platform.Photos.PHImageContentModeAspectFill
import platform.Photos.PHImageManager
import platform.Photos.PHImageRequestOptions
import platform.Photos.PHImageRequestOptionsDeliveryModeHighQualityFormat
import platform.Photos.PHPhotoLibrary
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberPhotoLibraryActions(
    onPhotosPicked: (List<SelectedPhoto>) -> Unit,
    onPhotosRecommended: (List<SelectedPhoto>) -> Unit,
    onMessage: (String) -> Unit,
): PhotoLibraryActions {
    val controller = remember { IosPhotoLibraryController() }
    controller.onPhotosPicked = onPhotosPicked
    controller.onPhotosRecommended = onPhotosRecommended
    controller.onMessage = onMessage

    return remember(controller) {
        PhotoLibraryActions(
            pickFromGallery = controller::presentPicker,
            recommendForLocation = controller::recommend,
        )
    }
}

private class IosPhotoLibraryController : NSObject(), PHPickerViewControllerDelegateProtocol {
    var onPhotosPicked: (List<SelectedPhoto>) -> Unit = {}
    var onPhotosRecommended: (List<SelectedPhoto>) -> Unit = {}
    var onMessage: (String) -> Unit = {}
    private var geocoder: CLGeocoder? = null

    fun presentPicker() {
        val presenter = topViewController() ?: run {
            onMessage("사진 선택 화면을 열지 못했어요.")
            return
        }
        val configuration = PHPickerConfiguration(PHPhotoLibrary.sharedPhotoLibrary()).apply {
            filter = PHPickerFilter.imagesFilter
            selectionLimit = MaxPhotosPerRecord.toLong()
        }
        val picker = PHPickerViewController(configuration)
        picker.delegate = this
        presenter.presentViewController(picker, animated = true, completion = null)
    }

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val results = didFinishPicking.filterIsInstance<PHPickerResult>()
        if (results.isEmpty()) return

        val loaded = MutableList<SelectedPhoto?>(results.size) { null }
        var remaining = results.size
        results.forEachIndexed { index, result ->
            loadPickerResult(result) { photo ->
                loaded[index] = photo
                remaining -= 1
                if (remaining == 0) {
                    val photos = loaded.filterNotNull()
                    if (photos.isEmpty()) {
                        onMessage("선택한 사진을 읽지 못했어요.")
                    } else {
                        onPhotosPicked(photos)
                    }
                }
            }
        }
    }

    fun recommend(location: Location, parentName: String?) {
        val status = PHPhotoLibrary.authorizationStatusForAccessLevel(PHAccessLevelReadWrite)
        when (status) {
            PHAuthorizationStatusAuthorized, PHAuthorizationStatusLimited -> {
                findRecommendations(location, parentName)
            }
            PHAuthorizationStatusNotDetermined -> {
                PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelReadWrite) { newStatus ->
                    onMain {
                        if (newStatus == PHAuthorizationStatusAuthorized || newStatus == PHAuthorizationStatusLimited) {
                            findRecommendations(location, parentName)
                        } else {
                            onMessage("장소 기반 추천을 사용하려면 사진 접근을 허용해 주세요.")
                        }
                    }
                }
            }
            else -> onMessage("장소 기반 추천을 사용하려면 설정에서 사진 접근을 허용해 주세요.")
        }
    }

    private fun findRecommendations(location: Location, parentName: String?) {
        geocoder?.cancelGeocode()
        geocoder = CLGeocoder().also { activeGeocoder ->
            activeGeocoder.geocodeAddressString(
                location.recommendationSearchText(parentName),
            ) { placemarks, _ ->
                val targetLocation = (placemarks?.firstOrNull() as? CLPlacemark)?.location
                if (targetLocation == null) {
                    onMessage("선택한 장소의 위치를 확인하지 못했어요.")
                    return@geocodeAddressString
                }
                findNearbyAssets(
                    targetLocation = targetLocation,
                    radiusMeters = location.recommendationRadiusMeters(),
                    target = location,
                    parentName = parentName,
                )
            }
        }
    }

    private fun findNearbyAssets(
        targetLocation: CLLocation,
        radiusMeters: Double,
        target: Location,
        parentName: String?,
    ) {
        val options = PHFetchOptions().apply {
            sortDescriptors = listOf(NSSortDescriptor("creationDate", ascending = false))
        }
        val result = PHAsset.fetchAssetsWithMediaType(PHAssetMediaTypeImage, options)
        val candidates = buildList {
            for (index in 0 until result.count.toInt()) {
                val asset = result.objectAtIndex(index.toULong()) as? PHAsset ?: continue
                val assetLocation = asset.location ?: continue
                val distance = assetLocation.distanceFromLocation(targetLocation)
                if (distance <= radiusMeters) add(asset to distance)
            }
        }
            .sortedBy { it.second }
            .take(MaxReverseGeocodeCandidates)
            .map { it.first }

        if (candidates.isEmpty()) {
            onPhotosRecommended(emptyList())
            return
        }
        filterAssetsBySelectedRegion(
            assets = candidates,
            target = target,
            parentName = parentName,
        ) { matchingAssets ->
            if (matchingAssets.isEmpty()) {
                onPhotosRecommended(emptyList())
            } else {
                loadAssets(matchingAssets, onPhotosRecommended)
            }
        }
    }

    private fun filterAssetsBySelectedRegion(
        assets: List<PHAsset>,
        target: Location,
        parentName: String?,
        index: Int = 0,
        matches: List<PHAsset> = emptyList(),
        completion: (List<PHAsset>) -> Unit,
    ) {
        if (index >= assets.size || matches.size >= MaxRecommendedPhotos) {
            completion(matches.take(MaxRecommendedPhotos))
            return
        }
        val asset = assets[index]
        val assetLocation = asset.location
        if (assetLocation == null) {
            filterAssetsBySelectedRegion(assets, target, parentName, index + 1, matches, completion)
            return
        }
        val activeGeocoder = geocoder ?: CLGeocoder().also { geocoder = it }
        activeGeocoder.reverseGeocodeLocation(assetLocation) { placemarks, _ ->
            val administrativeArea = (placemarks?.firstOrNull() as? CLPlacemark)
                ?.toAdministrativeArea()
            val nextMatches = if (administrativeArea?.matches(target, parentName) == true) {
                matches + asset
            } else {
                matches
            }
            filterAssetsBySelectedRegion(
                assets = assets,
                target = target,
                parentName = parentName,
                index = index + 1,
                matches = nextMatches,
                completion = completion,
            )
        }
    }

    private fun loadPickerResult(result: PHPickerResult, completion: (SelectedPhoto?) -> Unit) {
        val asset = result.assetIdentifier?.let(::assetForIdentifier)
        if (asset != null) {
            loadAsset(asset, completion)
            return
        }
        result.itemProvider.loadDataRepresentationForTypeIdentifier("public.image") { data, _ ->
            if (data == null) {
                completion(null)
                return@loadDataRepresentationForTypeIdentifier
            }
            val preview = UIImageJPEGRepresentation(UIImage(data), PreviewJpegQuality)
            onMain {
                completion(
                    preview?.let {
                        SelectedPhoto(
                            id = result.assetIdentifier ?: "ios-${data.hash}",
                            displayName = result.itemProvider.suggestedName ?: "여행 사진",
                            previewBytes = it.toByteArray(),
                        )
                    },
                )
            }
        }
    }

    private fun loadAssets(assets: List<PHAsset>, completion: (List<SelectedPhoto>) -> Unit) {
        val loaded = MutableList<SelectedPhoto?>(assets.size) { null }
        var remaining = assets.size
        assets.forEachIndexed { index, asset ->
            loadAsset(asset) { photo ->
                loaded[index] = photo
                remaining -= 1
                if (remaining == 0) completion(loaded.filterNotNull())
            }
        }
    }

    private fun loadAsset(asset: PHAsset, completion: (SelectedPhoto?) -> Unit) {
        val options = PHImageRequestOptions().apply {
            // The default delivery mode is opportunistic, so Photos can invoke the
            // callback first with a degraded preview and then with the final image.
            // We store the callback result as the preview bytes, so accepting that
            // first callback leaves the selected photo permanently pixelated.
            deliveryMode = PHImageRequestOptionsDeliveryModeHighQualityFormat
            networkAccessAllowed = true
        }
        var didComplete = false
        PHImageManager.defaultManager().requestImageForAsset(
            asset = asset,
            targetSize = CGSizeMake(PreviewSize, PreviewSize),
            contentMode = PHImageContentModeAspectFill,
            options = options,
        ) { image, _ ->
            val data = image?.let { UIImageJPEGRepresentation(it, PreviewJpegQuality) }
            val coordinate = asset.location?.coordinate
            val latitude = coordinate?.useContents { latitude }
            val longitude = coordinate?.useContents { longitude }
            onMain {
                // Keep this guard even with high-quality delivery: an asset may
                // still produce more than one callback when an iCloud download
                // fails or is cancelled.
                if (didComplete) return@onMain
                didComplete = true
                completion(
                    data?.let {
                        SelectedPhoto(
                            id = asset.localIdentifier,
                            displayName = asset.displayName(),
                            previewBytes = it.toByteArray(),
                            latitude = latitude,
                            longitude = longitude,
                            capturedAt = asset.creationDate?.formattedPhotoDate(),
                        )
                    },
                )
            }
        }
    }

    private fun assetForIdentifier(identifier: String): PHAsset? =
        PHAsset.fetchAssetsWithLocalIdentifiers(listOf(identifier), null).firstObject as? PHAsset
}

private fun PHAsset.displayName(): String =
    (PHAssetResource.assetResourcesForAsset(this).firstOrNull() as? PHAssetResource)
        ?.originalFilename
        ?: "여행 사진"

private fun CLPlacemark.toAdministrativeArea(): PhotoAdministrativeArea = PhotoAdministrativeArea(
    countryCode = ISOcountryCode,
    administrativeArea = administrativeArea,
    subAdministrativeArea = subAdministrativeArea,
    locality = locality,
    subLocality = subLocality,
)

private fun NSDate.formattedPhotoDate(): String = NSDateFormatter().run {
    dateFormat = "yyyy.MM.dd"
    stringFromDate(this@formattedPhotoDate)
}

private fun NSData.toByteArray(): ByteArray {
    if (length == 0UL) return ByteArray(0)
    return ByteArray(length.toInt()).also { bytes ->
        bytes.usePinned { pinned -> getBytes(pinned.addressOf(0), length) }
    }
}

private fun topViewController(): UIViewController? {
    val application = UIApplication.sharedApplication
    val window = application.keyWindow
        ?: application.windows.filterIsInstance<UIWindow>().firstOrNull { it.isKeyWindow() }
    var controller = window?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}

private fun onMain(block: () -> Unit) {
    dispatch_async(dispatch_get_main_queue(), block)
}

private const val PreviewSize = 960.0
private const val PreviewJpegQuality = 0.84
private const val MaxReverseGeocodeCandidates = 60
private const val MaxRecommendedPhotos = 12
