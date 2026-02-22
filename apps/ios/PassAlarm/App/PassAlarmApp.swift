import SwiftUI

@main
struct PassAlarmApp: App {
    @State private var container = DIContainer.shared

    var body: some Scene {
        WindowGroup {
            RootView()
                .environment(container)
        }
    }
}
