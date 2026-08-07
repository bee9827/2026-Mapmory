import AVFoundation
import Photos

enum MediaPermissionStatus: Equatable {
    case notDetermined
    case authorized
    case limited
    case denied
    case restricted
}

enum MediaPermissionManager {
    static func cameraStatus() -> MediaPermissionStatus {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .notDetermined:
            return .notDetermined
        case .authorized:
            return .authorized
        case .denied:
            return .denied
        case .restricted:
            return .restricted
        @unknown default:
            return .denied
        }
    }

    static func requestCameraAccess() async -> MediaPermissionStatus {
        guard cameraStatus() == .notDetermined else {
            return cameraStatus()
        }

        let granted = await AVCaptureDevice.requestAccess(for: .video)
        return granted ? .authorized : .denied
    }

    static func photoLibraryStatus(
        for accessLevel: PHAccessLevel = .readWrite
    ) -> MediaPermissionStatus {
        mapPhotoLibraryStatus(
            PHPhotoLibrary.authorizationStatus(for: accessLevel)
        )
    }

    static func requestPhotoLibraryAccess() async -> MediaPermissionStatus {
        await requestPhotoLibraryAccess(for: .readWrite)
    }

    static func requestPhotoLibraryAddAccess() async -> MediaPermissionStatus {
        await requestPhotoLibraryAccess(for: .addOnly)
    }

    private static func requestPhotoLibraryAccess(
        for accessLevel: PHAccessLevel
    ) async -> MediaPermissionStatus {
        let currentStatus = photoLibraryStatus(for: accessLevel)
        guard currentStatus == .notDetermined else {
            return currentStatus
        }

        let status = await withCheckedContinuation { continuation in
            PHPhotoLibrary.requestAuthorization(for: accessLevel) { status in
                continuation.resume(returning: status)
            }
        }
        return mapPhotoLibraryStatus(status)
    }

    private static func mapPhotoLibraryStatus(
        _ status: PHAuthorizationStatus
    ) -> MediaPermissionStatus {
        switch status {
        case .notDetermined:
            return .notDetermined
        case .authorized:
            return .authorized
        case .limited:
            return .limited
        case .denied:
            return .denied
        case .restricted:
            return .restricted
        @unknown default:
            return .denied
        }
    }
}
