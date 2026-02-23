import SwiftUI

private enum OnboardingStep {
    case permission
    case seeding
    case tutorial
    case done
}

struct OnboardingView: View {
    @Environment(DIContainer.self) private var container
    var onComplete: () -> Void

    @State private var step: OnboardingStep = .permission
    @State private var requesting = false
    @State private var denied = false
    @State private var showToast = false
    @State private var toastMessage = ""
    @State private var arrowOffset: CGFloat = 0

    var body: some View {
        ZStack {
            MapBackdrop(timeOfDay: .morning)

            VStack(spacing: PassSpacing.lg) {
                Spacer()

                switch step {
                case .permission:
                    permissionStep
                case .seeding:
                    seedingStep
                case .tutorial:
                    tutorialStep
                case .done:
                    EmptyView()
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

    // MARK: - Step 1: Permission

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
        }
    }

    // MARK: - Step 2: Seeding

    private var seedingStep: some View {
        VStack(spacing: PassSpacing.lg) {
            ProgressView()
                .tint(.white)
                .scaleEffect(1.5)

            Text("アラームを準備中...")
                .font(.title3)
                .fontWeight(.semibold)
                .foregroundStyle(.white)

            Text("デフォルトのアラームを設定しています")
                .font(.subheadline)
                .foregroundStyle(.white.opacity(0.6))
        }
        .task {
            await seedAlarms()
        }
    }

    // MARK: - Step 3: Tutorial

    private var tutorialStep: some View {
        VStack(spacing: PassSpacing.lg) {
            Image(systemName: "hand.draw")
                .font(.system(size: 64))
                .foregroundStyle(.white.opacity(0.8))

            Text("スワイプしてスキップしてみよう")
                .font(.title3)
                .fontWeight(.bold)
                .foregroundStyle(.white)
                .multilineTextAlignment(.center)

            // Animated swipe hint
            HStack(spacing: PassSpacing.sm) {
                Image(systemName: "arrow.right")
                    .font(.system(size: 24, weight: .semibold))
                    .foregroundStyle(.white.opacity(0.7))
                    .offset(x: arrowOffset)

                Text("右にスワイプでパス")
                    .font(.subheadline)
                    .foregroundStyle(.white.opacity(0.6))
            }
            .onAppear {
                withAnimation(
                    .easeInOut(duration: 1.0)
                    .repeatForever(autoreverses: true)
                ) {
                    arrowOffset = 20
                }
            }

            Spacer()
                .frame(height: PassSpacing.lg)

            PassButton(
                title: "はじめる",
                size: .large,
                color: PassColors.brand,
                haptic: .success
            ) {
                withAnimation {
                    step = .done
                }
                onComplete()
            }
            .padding(.horizontal, PassSpacing.lg)
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
                    step = .seeding
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

    private func seedAlarms() async {
        do {
            try await container.seedDefaultAlarmsUseCase.execute()
        } catch {
            print("Seed alarms error: \(error)")
        }
        try? await Task.sleep(for: .seconds(1))
        withAnimation {
            step = .tutorial
        }
    }
}
