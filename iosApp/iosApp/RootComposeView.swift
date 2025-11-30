import SwiftUI
import shared

struct RootComposeView: UIViewControllerRepresentable {

    private let root: Root
    private let backDispatcher: BackDispatcher

    init(root: Root, backDispatcher: BackDispatcher) {
        self.root = root
        self.backDispatcher = backDispatcher
    }

    func makeUIViewController(context: Context) -> UIViewController {

        RootViewControllerKt.RootViewController(root: root, backDispatcher: backDispatcher)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
