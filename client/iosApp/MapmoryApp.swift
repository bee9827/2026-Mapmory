import FirebaseCore
import Shared
import SwiftUI
import UIKit
import UserNotifications

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
    @UIApplicationDelegateAdaptor(UploadAppDelegate.self) private var appDelegate
    @State private var isDarkTheme = false
    private let analyticsLogger: FirebaseAnalyticsLogger

    init() {
#if INTERNAL
        analyticsLogger = FirebaseAnalyticsLogger(isEnabled: false)
#else
        FirebaseApp.configure()
        analyticsLogger = FirebaseAnalyticsLogger()
#endif
    }

    var body: some Scene {
        WindowGroup {
            ZStack {
                Color(uiColor: isDarkTheme ? darkSystemBarColor : lightSystemBarColor)
                ComposeView(
                    isDarkTheme: $isDarkTheme,
                    analyticsLogger: analyticsLogger,
                )
            }
            .ignoresSafeArea()
            .preferredColorScheme(isDarkTheme ? .dark : .light)
        }
    }
}

private struct ComposeView: UIViewControllerRepresentable {
    @Binding var isDarkTheme: Bool
    let analyticsLogger: FirebaseAnalyticsLogger

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

final class UploadAppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        return true
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([.banner, .sound])
    }

    func application(_ application: UIApplication, handleEventsForBackgroundURLSession identifier: String, completionHandler: @escaping () -> Void) {
        guard identifier == "com.mapmory.record-uploads" else {
            completionHandler()
            return
        }
        MainViewControllerKt.resumeBackgroundUploads(
            apiBaseUrl: AppConfiguration.apiBaseUrl,
            tokenStore: KeychainAuthTokenStore(apiBaseUrl: AppConfiguration.apiBaseUrl)
        )
        PhotoUploadTransferKt.handleBackgroundUploadEvents(completion: completionHandler)
    }
}
