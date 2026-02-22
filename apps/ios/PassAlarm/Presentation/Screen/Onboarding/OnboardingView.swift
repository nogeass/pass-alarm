import SwiftUI

struct OnboardingView: View {
    @Environment(DIContainer.self) private var container
    var onComplete: () -> Void

    @State private var requesting = false
    @State private var denied = false
    @State private var showToast = false
    @State private var toastMessage = ""

    var body: some View {
        ZStack {
            MapBackdrop(timeOfDay: .morning)

            VStack(spacing: PassSpacing.lg) {
                Spacer()

                Text("⏰")
                    .font(.system(size: 80))

                Text("パスアラーム")
                    .font(.largeTitle)
                    .fontWeight(.black)
                    .foregroundStyle(.white)

                Text("OFFしない。今日だけスキップ。")
                    .font(.title3)
                    .foregroundStyle(.white.opacity(0.7))

                Spacer()

                if denied {
                    VStack(spacing: PassSpacing.md) {
                        Text("通知がONじゃないと起こせない")
                            .font(.headline)
                            .foregroundStyle(.white)

                        Text("設定アプリから通知を許可してください")
                            .font(.subheadline)
                            .foregroundStyle(.white.opacity(0.6))

                        PassButton(
                            title: "設定を開く",
                            size: .medium,
                            color: PassColors.brand
                        ) {
                            if let url = URL(string: UIApplication.openSettingsURLString) {
                                UIApplication.shared.open(url)
                            }
                        }
                        .padding(.horizontal, PassSpacing.xl)
                    }
                } else {
                    PassButton(
                        title: "通知を許可して始める",
                        size: .large,
                        color: PassColors.brand,
                        isEnabled: !requesting,
                        haptic: .success
                    ) {
                        Task { await requestPermission() }
                    }
                    .padding(.horizontal, PassSpacing.lg)
                }

                Spacer()
            }
            .padding()

            VStack {
                Spacer()
                PraiseToast(message: toastMessage, isVisible: $showToast)
                    .padding(.bottom, PassSpacing.xl)
            }
        }
    }

    private func requestPermission() async {
        requesting = true
        do {
            let granted = try await container.notificationPermission.request()
            if granted {
                toastMessage = "いいね これで起こせる"
                withAnimation(.spring(response: 0.3)) {
                    showToast = true
                }
                try? await Task.sleep(for: .seconds(1))
                onComplete()
            } else {
                denied = true
                PassHaptics.warning()
            }
        } catch {
            denied = true
            PassHaptics.warning()
        }
        requesting = false
    }
}
