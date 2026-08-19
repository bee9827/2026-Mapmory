import Shared
import SwiftUI

private let systemBarColor = Color(
    red: 17.0 / 255.0,
    green: 21.0 / 255.0,
    blue: 24.0 / 255.0
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
        }
    }
}

private struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let viewController = MainViewControllerKt.MainViewController()
        viewController.view.backgroundColor = UIColor(
            red: 17.0 / 255.0,
            green: 21.0 / 255.0,
            blue: 24.0 / 255.0,
            alpha: 1,
        )
        return viewController
    }

    func updateUIViewController(
        _ uiViewController: UIViewController,
        context: Context
    ) {}
}
