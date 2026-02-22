import SwiftUI

enum PassButtonSize {
    case large, medium, small

    var height: CGFloat {
        switch self {
        case .large: 64
        case .medium: 48
        case .small: 36
        }
    }

    var font: Font {
        switch self {
        case .large: .system(size: 20, weight: .bold)
        case .medium: .system(size: 16, weight: .bold)
        case .small: .system(size: 14, weight: .semibold)
        }
    }

    var cornerRadius: CGFloat {
        switch self {
        case .large: 32
        case .medium: 24
        case .small: 18
        }
    }
}

struct PassButton: View {
    let title: String
    var size: PassButtonSize = .medium
    var color: Color = PassColors.brand
    var isEnabled: Bool = true
    var haptic: PassHapticType = .tap
    let action: () -> Void

    @State private var isPressed = false
    @State private var rippleScale: CGFloat = 0
    @State private var rippleOpacity: Double = 0

    enum PassHapticType {
        case tap, medium, success
    }

    var body: some View {
        Button(action: {
            fireHaptic()
            withAnimation(.spring(response: 0.2)) {
                rippleScale = 1.5
                rippleOpacity = 0.3
            }
            withAnimation(.easeOut(duration: 0.4).delay(0.1)) {
                rippleScale = 2.0
                rippleOpacity = 0
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                rippleScale = 0
            }
            action()
        }) {
            Text(title)
                .font(size.font)
                .foregroundStyle(.white)
                .frame(maxWidth: .infinity)
                .frame(height: size.height)
                .background(
                    ZStack {
                        RoundedRectangle(cornerRadius: size.cornerRadius)
                            .fill(isEnabled ? color : color.opacity(0.4))

                        // Ripple
                        Circle()
                            .fill(Color.white.opacity(rippleOpacity))
                            .scaleEffect(rippleScale)
                    }
                    .clipShape(RoundedRectangle(cornerRadius: size.cornerRadius))
                )
                .shadow(
                    color: color.opacity(isPressed ? 0.4 : 0.2),
                    radius: isPressed ? 8 : 4,
                    y: isPressed ? 2 : 4
                )
                .scaleEffect(isPressed ? 0.98 : 1.0)
        }
        .disabled(!isEnabled)
        .buttonStyle(.plain)
        .simultaneousGesture(
            DragGesture(minimumDistance: 0)
                .onChanged { _ in
                    withAnimation(.easeInOut(duration: 0.1)) { isPressed = true }
                }
                .onEnded { _ in
                    withAnimation(.spring(response: 0.3)) { isPressed = false }
                }
        )
    }

    private func fireHaptic() {
        switch haptic {
        case .tap: PassHaptics.tap()
        case .medium: PassHaptics.medium()
        case .success: PassHaptics.success()
        }
    }
}
