import Shared
import SwiftUI

private let systemBarColor = Color(
    red: 250.0 / 255.0,
    green: 252.0 / 255.0,
    blue: 251.0 / 255.0
)

@main
struct MapmoryApp: App {
    var body: some Scene {
        WindowGroup {
            ZStack {
                systemBarColor
                ComposeView()
            }
            .ignoresSafeArea()
            .preferredColorScheme(.light)
        }
    }
}

private struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let viewController = MainViewControllerKt.MainViewController()
        viewController.view.backgroundColor = UIColor(
            red: 250.0 / 255.0,
            green: 252.0 / 255.0,
            blue: 251.0 / 255.0,
            alpha: 1,
        )
        return viewController
    }

    func updateUIViewController(
        _ uiViewController: UIViewController,
        context: Context
    ) {}
}
