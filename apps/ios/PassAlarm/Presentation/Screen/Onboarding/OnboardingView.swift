import SwiftUI

private enum OnboardingStep {
    case permission
    case tutorial
    case done
}

struct OnboardingView: View {
    @Environment(DIContainer.self) private var container
    var skipPermission: Bool = false
    var onComplete: () -> Void

    @State private var step: OnboardingStep = .permission
    @State private var requesting = false
    @State private var denied = false
    @State private var showToast = false
    @State private var toastMessage = ""

    var body: some View {
        ZStack {
            MapBackdrop(timeOfDay: .morning)

            switch step {
            case .permission:
                VStack(spacing: PassSpacing.lg) {
                    Spacer()
                    permissionStep
                    Spacer()
                }
                .padding()
            case .tutorial:
                TutorialFlowView(onComplete: {
                    withAnimation {
                        step = .done
                    }
                    onComplete()
                })
            case .done:
                EmptyView()
            }

            VStack {
                Spacer()
                PraiseToast(message: toastMessage, isVisible: $showToast)
                    .padding(.bottom, PassSpacing.xl)
            }
        }
        .onAppear {
            if skipPermission {
                step = .tutorial
            }
        }
    }

    // MARK: - Permission Step

    private var permissionStep: some View {
        VStack(spacing: PassSpacing.lg) {
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
                .frame(height: PassSpacing.md)

            if denied {
                VStack(spacing: PassSpacing.md) {
                    Text("通知がOFFだとアラームが鳴りません")
                        .font(.headline)
                        .foregroundStyle(.white)

                    Text("あとから設定アプリで許可できます")
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

                    Button {
                        withAnimation { step = .tutorial }
                    } label: {
                        Text("あとで設定する")
                            .font(.subheadline)
                            .foregroundStyle(.white.opacity(0.5))
                    }
                }
            } else {
                VStack(spacing: PassSpacing.md) {
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

                    Button {
                        withAnimation { step = .tutorial }
                    } label: {
                        Text("あとで設定する")
                            .font(.subheadline)
                            .foregroundStyle(.white.opacity(0.5))
                    }
                }
            }
        }
    }

    // MARK: - Actions

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
                withAnimation {
                    step = .tutorial
                }
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
