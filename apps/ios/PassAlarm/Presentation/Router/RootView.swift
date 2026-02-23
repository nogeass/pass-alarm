import SwiftUI

struct RootView: View {
    @Environment(DIContainer.self) private var container
    @State private var permissionGranted = false
    @State private var checkingPermission = true

    var body: some View {
        Group {
            if checkingPermission {
                ProgressView()
            } else if !permissionGranted {
                OnboardingView(onComplete: {
                    permissionGranted = true
                })
            } else {
                MainContentView()
            }
        }
        .task {
            let status = await container.notificationPermission.currentStatus()
            permissionGranted = (status == .authorized)
            checkingPermission = false
        }
    }
}
