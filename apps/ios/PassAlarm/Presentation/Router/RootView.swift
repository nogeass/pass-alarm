import SwiftUI

struct RootView: View {
    @Environment(DIContainer.self) private var container
    @State private var permissionGranted = false
    @State private var tutorialCompleted = false
    @State private var checkingState = true

    var body: some View {
        Group {
            if checkingState {
                ProgressView()
            } else if !permissionGranted || !tutorialCompleted {
                OnboardingView(
                    skipPermission: permissionGranted,
                    onComplete: {
                        permissionGranted = true
                        tutorialCompleted = true
                    }
                )
            } else {
                MainContentView()
            }
        }
        .task {
            let status = await container.notificationPermission.currentStatus()
            permissionGranted = (status == .authorized)
            var settings = await container.appSettingsRepository.get()

            // Migrate existing users: if alarms exist, skip tutorial
            if !settings.tutorialCompleted {
                let plans = (try? await container.alarmPlanRepository.fetchAll()) ?? []
                if !plans.isEmpty {
                    settings.tutorialCompleted = true
                    await container.appSettingsRepository.save(settings)
                }
            }

            tutorialCompleted = settings.tutorialCompleted
            checkingState = false
        }
    }
}
