import SwiftUI

struct AlarmRingingView: View {
    @Binding var session: AlarmSession?
    var onStop: () -> Void
    var onSnooze: () -> Void

    @State private var showToast = false
    @State private var toastMessage = ""
    @State private var wakeUpTriggered = false

    var body: some View {
        ZStack {
            // Night-to-morning gradient transition
            ZStack {
                LinearGradient(
                    colors: [PassColors.nightStart, PassColors.nightEnd],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                LinearGradient(
                    colors: [PassColors.morningStart, PassColors.morningEnd],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                .opacity(wakeUpTriggered ? 1 : 0)
            }
            .ignoresSafeArea()

            VStack(spacing: PassSpacing.xxl) {
                Spacer()

                Text("⏰")
                    .font(.system(size: 80))

                if let session {
                    Text(session.progressText)
                        .font(.title2)
                        .foregroundStyle(.white.opacity(0.6))
                }

                Text("アラーム")
                    .font(.system(size: 36, weight: .bold))
                    .foregroundStyle(.white)

                Spacer()

                // Stop button (primary, large)
                VStack(spacing: PassSpacing.lg) {
                    Button {
                        PassHaptics.success()
                        withAnimation(.easeInOut(duration: 1.0)) {
                            wakeUpTriggered = true
                        }
                        if let msg = PraiseMessages.randomWakeUp() {
                            toastMessage = msg
                            withAnimation(.spring(response: 0.3)) {
                                showToast = true
                            }
                        }
                        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                            onStop()
                        }
                    } label: {
                        VStack(spacing: PassSpacing.sm) {
                            Image(systemName: "sun.max.fill")
                                .font(.system(size: 36))
                            Text("起きた")
                                .font(.system(size: 18, weight: .bold))
                        }
                        .foregroundStyle(.white)
                        .frame(width: 120, height: 120)
                        .background(
                            Circle()
                                .fill(PassColors.stopRed)
                                .shadow(color: PassColors.stopRed.opacity(0.5), radius: 16, y: 4)
                        )
                    }

                    // Snooze button (secondary)
                    Button {
                        PassHaptics.tap()
                        onSnooze()
                    } label: {
                        HStack(spacing: PassSpacing.sm) {
                            Image(systemName: "moon.zzz")
                                .font(.system(size: 18))
                            Text("スヌーズ")
                                .font(.system(size: 16, weight: .semibold))
                        }
                        .foregroundStyle(.white)
                        .padding(.horizontal, PassSpacing.xl)
                        .padding(.vertical, PassSpacing.md)
                        .background(
                            Capsule()
                                .fill(PassColors.snoozeAmber.opacity(0.8))
                        )
                    }
                }

                Spacer()
            }

            VStack {
                Spacer()
                PraiseToast(message: toastMessage, isVisible: $showToast)
                    .padding(.bottom, PassSpacing.xxl)
            }
        }
    }
}
