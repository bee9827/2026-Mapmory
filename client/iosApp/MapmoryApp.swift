import Shared
import SwiftUI

@main
struct MapmoryApp: App {
    var body: some Scene {
        WindowGroup {
            ZStack(alignment: .bottom) {
                ComposeView()
                    .ignoresSafeArea()

                MediaPermissionView()
                    .padding()
            }
        }
    }
}

private struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let viewController = MainViewControllerKt.MainViewController()
        viewController.view.backgroundColor = .systemBackground
        return viewController
    }

    func updateUIViewController(
        _ uiViewController: UIViewController,
        context: Context
    ) {}
}
