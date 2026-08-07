import SwiftUI
import UIKit

@MainActor
final class MediaPermissionViewModel: ObservableObject {
    @Published private(set) var cameraStatus = MediaPermissionManager.cameraStatus()
    @Published private(set) var photoLibraryStatus = MediaPermissionManager.photoLibraryStatus()
    @Published private(set) var isRequestingCamera = false
    @Published private(set) var isRequestingPhotoLibrary = false

    func refresh() {
        cameraStatus = MediaPermissionManager.cameraStatus()
        photoLibraryStatus = MediaPermissionManager.photoLibraryStatus()
    }

    func requestCamera() async {
        guard cameraStatus == .notDetermined else { return }

        isRequestingCamera = true
        cameraStatus = await MediaPermissionManager.requestCameraAccess()
        isRequestingCamera = false
    }

    func requestPhotoLibrary() async {
        guard photoLibraryStatus == .notDetermined else { return }

        isRequestingPhotoLibrary = true
        photoLibraryStatus = await MediaPermissionManager.requestPhotoLibraryAccess()
        isRequestingPhotoLibrary = false
    }
}

struct MediaPermissionView: View {
    @Environment(\.openURL) private var openURL
    @Environment(\.scenePhase) private var scenePhase
    @StateObject private var viewModel = MediaPermissionViewModel()

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("사진 접근 권한")
                .font(.headline)

            permissionRow(
                title: "카메라",
                status: viewModel.cameraStatus,
                isRequesting: viewModel.isRequestingCamera
            ) {
                handleCameraAction()
            }

            permissionRow(
                title: "사진 보관함",
                status: viewModel.photoLibraryStatus,
                isRequesting: viewModel.isRequestingPhotoLibrary
            ) {
                handlePhotoLibraryAction()
            }

            if viewModel.photoLibraryStatus == .limited {
                Text("일부 사진만 허용되어 있습니다. 전체 갤러리 탐색이 필요하면 설정에서 ‘전체 접근’을 선택해 주세요.")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
        .padding(20)
        .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 20))
        .shadow(radius: 8)
        .onChange(of: scenePhase) { phase in
            if phase == .active {
                viewModel.refresh()
            }
        }
    }

    private func permissionRow(
        title: String,
        status: MediaPermissionStatus,
        isRequesting: Bool,
        action: @escaping () -> Void
    ) -> some View {
        HStack(spacing: 12) {
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.body.weight(.semibold))
                Text(status.description)
                    .font(.caption)
                    .foregroundStyle(status.tintColor)
            }

            Spacer()

            Button(status.actionTitle, action: action)
                .buttonStyle(.borderedProminent)
                .disabled(status == .authorized || status == .restricted || isRequesting)
        }
    }

    private func handleCameraAction() {
        if viewModel.cameraStatus == .notDetermined {
            Task { await viewModel.requestCamera() }
        } else {
            openSettings()
        }
    }

    private func handlePhotoLibraryAction() {
        if viewModel.photoLibraryStatus == .notDetermined {
            Task { await viewModel.requestPhotoLibrary() }
        } else {
            openSettings()
        }
    }

    private func openSettings() {
        guard let settingsURL = URL(string: UIApplication.openSettingsURLString) else {
            return
        }
        openURL(settingsURL)
    }
}

private extension MediaPermissionStatus {
    var description: String {
        switch self {
        case .notDetermined:
            return "요청 전"
        case .authorized:
            return "허용됨"
        case .limited:
            return "일부 사진만 허용됨"
        case .denied:
            return "거부됨"
        case .restricted:
            return "기기 정책으로 제한됨"
        }
    }

    var actionTitle: String {
        switch self {
        case .notDetermined:
            return "권한 요청"
        case .authorized:
            return "허용됨"
        case .limited, .denied:
            return "설정 열기"
        case .restricted:
            return "제한됨"
        }
    }

    var tintColor: Color {
        switch self {
        case .authorized:
            return .green
        case .limited:
            return .orange
        case .notDetermined:
            return .secondary
        case .denied, .restricted:
            return .red
        }
    }
}
