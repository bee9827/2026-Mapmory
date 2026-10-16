import FirebaseCore
import FirebaseRemoteConfig
import Shared
import SwiftUI
import UIKit

private let lightSystemBarColor = UIColor(
    red: 250.0 / 255.0,
    green: 252.0 / 255.0,
    blue: 251.0 / 255.0,
    alpha: 1
)
private let darkSystemBarColor = UIColor(
    red: 17.0 / 255.0,
    green: 21.0 / 255.0,
    blue: 24.0 / 255.0,
    alpha: 1
)

private enum AppConfiguration {
    static let apiBaseUrl: String = {
        guard let value = Bundle.main.object(forInfoDictionaryKey: "MAPMORY_API_BASE_URL") as? String,
              !value.isEmpty else {
            fatalError("MAPMORY_API_BASE_URL is missing from the app configuration")
        }
        return value
    }()
}

@main
struct MapmoryApp: App {
    @State private var isDarkTheme = false
    @Environment(\.scenePhase) private var scenePhase
    private let forceUpdate: IosForceUpdateController
    private let analyticsLogger: FirebaseAnalyticsLogger

    init() {
#if INTERNAL
        analyticsLogger = FirebaseAnalyticsLogger(isEnabled: false)
#else
        FirebaseApp.configure()
        analyticsLogger = FirebaseAnalyticsLogger()
#endif
        forceUpdate = IosForceUpdateController()
    }

    var body: some Scene {
        WindowGroup {
            ZStack {
                Color(uiColor: isDarkTheme ? darkSystemBarColor : lightSystemBarColor)
                ComposeView(
                    isDarkTheme: $isDarkTheme,
                    analyticsLogger: analyticsLogger,
                    forceUpdate: forceUpdate,
                )
            }
            .onAppear { forceUpdate.refresh() }
            .onChange(of: scenePhase) { phase in
                if phase == .active { forceUpdate.refresh() }
            }
            .ignoresSafeArea()
            .preferredColorScheme(isDarkTheme ? .dark : .light)
        }
    }
}

private struct ComposeView: UIViewControllerRepresentable {
    @Binding var isDarkTheme: Bool
    let analyticsLogger: FirebaseAnalyticsLogger
    let forceUpdate: IosForceUpdateController

    func makeCoordinator() -> Coordinator {
        Coordinator(isDarkTheme: $isDarkTheme)
    }

    func makeUIViewController(context: Context) -> UIViewController {
        let coordinator = context.coordinator
        let navigation = MapmoryNavigation()
        let composeViewController = MainViewControllerKt.MainViewController(
            apiBaseUrl: AppConfiguration.apiBaseUrl,
            onThemeChanged: { isDark in
                coordinator.updateTheme(isDark.boolValue)
            },
            analytics: analyticsLogger,
            tokenStore: KeychainAuthTokenStore(apiBaseUrl: AppConfiguration.apiBaseUrl),
            navigation: navigation,
            updateState: forceUpdate.state,
            onRetryUpdateCheck: { forceUpdate.refresh() },
        )
        let viewController = composeViewController
        applyTheme(to: viewController)
        return viewController
    }

    func updateUIViewController(
        _ uiViewController: UIViewController,
        context: Context
    ) {
        context.coordinator.isDarkTheme = $isDarkTheme
        applyTheme(to: uiViewController)
    }

    private func applyTheme(to viewController: UIViewController) {
        viewController.overrideUserInterfaceStyle = isDarkTheme ? .dark : .light
        viewController.view.backgroundColor = isDarkTheme ? darkSystemBarColor : lightSystemBarColor
        viewController.setNeedsStatusBarAppearanceUpdate()
    }

    final class Coordinator {
        var isDarkTheme: Binding<Bool>

        init(isDarkTheme: Binding<Bool>) {
            self.isDarkTheme = isDarkTheme
        }

        func updateTheme(_ isDark: Bool) {
            DispatchQueue.main.async { [weak self] in
                self?.isDarkTheme.wrappedValue = isDark
            }
        }
    }
}


private final class IosForceUpdateController {
    let state = ForceUpdateState(
        currentVersion: Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "",
        storeUrl: "https://apps.apple.com/app/id6807056166"
    )
#if DEBUG || INTERNAL
    private let key = "ios_dev_min_supported_version"
#else
    private let key = "ios_min_supported_version"
#endif
    private var fetching = false
    private var generation = 0

    init() {
        _ = state.applyMinimumVersion(value: UserDefaults.standard.string(forKey: key) ?? "")
    }

    func refresh() {
        guard !fetching else { return }
        guard FirebaseApp.app() != nil else { state.finishCheck(); return }
        fetching = true
        generation += 1
        let request = generation
        let remote = RemoteConfig.remoteConfig()
        let settings = RemoteConfigSettings()
#if DEBUG
        settings.minimumFetchInterval = 0
#else
        settings.minimumFetchInterval = 3600
#endif
        settings.fetchTimeout = 10
        remote.configSettings = settings
        DispatchQueue.main.asyncAfter(deadline: .now() + 12) { [weak self] in
            guard let self, self.generation == request else { return }
            self.generation += 1
            self.fetching = false
            self.state.finishCheck()
        }
        remote.ensureInitialized { [weak self] _ in
            DispatchQueue.main.async {
                guard let self, self.generation == request else { return }
                self.accept(remote.configValue(forKey: self.key).stringValue)
                remote.fetchAndActivate { [weak self] _, error in
                    DispatchQueue.main.async {
                        guard let self, self.generation == request else { return }
                        if error == nil { self.accept(remote.configValue(forKey: self.key).stringValue) }
                        self.generation += 1
                        self.fetching = false
                        self.state.finishCheck()
                    }
                }
            }
        }
    }

    private func accept(_ value: String) {
        if state.applyMinimumVersion(value: value) {
            UserDefaults.standard.set(value, forKey: key)
        }
    }
}
