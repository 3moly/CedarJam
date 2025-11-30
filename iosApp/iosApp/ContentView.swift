import SwiftUI
import shared

struct ContentView: View {
    private var root: Root
    private let backDispatcher: BackDispatcher

    init(root: Root, backDispatcher: BackDispatcher) {
        self.root = root
        self.backDispatcher = backDispatcher
    }

    var body: some View {
          RootComposeView(root:root,backDispatcher: backDispatcher)
            .ignoresSafeArea(.all, edges: .bottom)
            .edgesIgnoringSafeArea([.top,.bottom])
    }
}
