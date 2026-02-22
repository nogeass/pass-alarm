import SwiftUI

struct PassToggle: View {
    @Binding var isOn: Bool
    var onMessage: String = "OK、明日も起こす"
    var offMessage: String = "今日はおやすみモード"

    @State private var showMessage = false
    @State private var displayMessage = ""

    var body: some View {
        VStack(spacing: PassSpacing.sm) {
            Button {
                PassHaptics.medium()
                withAnimation(.spring(response: 0.4, dampingFraction: 0.7)) {
                    isOn.toggle()
                }
                displayMessage = isOn ? onMessage : offMessage
                withAnimation(.easeIn(duration: 0.2)) {
                    showMessage = true
                }
                DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                    withAnimation(.easeOut(duration: 0.3)) {
                        showMessage = false
                    }
                }
            } label: {
                ZStack {
                    // Background: day/night
                    RoundedRectangle(cornerRadius: 28)
                        .fill(
                            LinearGradient(
                                colors: isOn
                                    ? [PassColors.morningStart, PassColors.morningEnd]
                                    : [PassColors.nightStart, PassColors.nightEnd],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .frame(width: 72, height: 40)

                    // Thumb
                    Circle()
                        .fill(.white)
                        .frame(width: 32, height: 32)
                        .shadow(color: .black.opacity(0.15), radius: 4, y: 2)
                        .offset(x: isOn ? 16 : -16)

                    // Moon icon when off
                    if !isOn {
                        Image(systemName: "moon.fill")
                            .font(.system(size: 12))
                            .foregroundStyle(.white.opacity(0.6))
                            .offset(x: 16)
                    }
                }
            }
            .buttonStyle(.plain)

            if showMessage {
                Text(displayMessage)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundStyle(.secondary)
                    .transition(.opacity.combined(with: .scale(scale: 0.9)))
            }
        }
    }
}
