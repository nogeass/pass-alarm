import SwiftUI

@main
struct PassAlarmApp: App {
    @State private var container = DIContainer.shared
    @State private var redeemToken: String?
    @State private var showRedeem = false

    var body: some Scene {
        WindowGroup {
            RootView()
                .environment(container)
                .onOpenURL { url in
                    handleDeepLink(url)
                }
                .sheet(isPresented: $showRedeem) {
                    RedeemView(token: redeemToken)
                        .environment(container)
                }
        }
    }

    private func handleDeepLink(_ url: URL) {
        // Handle https://pass-alarm.nogeass.com/r/{token}
        guard url.host == "pass-alarm.nogeass.com",
              url.pathComponents.count >= 3,
              url.pathComponents[1] == "r" else {
            return
        }
        let token = url.pathComponents[2]
        redeemToken = token
        showRedeem = true
    }
}
