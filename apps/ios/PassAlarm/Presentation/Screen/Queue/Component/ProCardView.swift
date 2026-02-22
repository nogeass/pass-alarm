import SwiftUI

struct ProCardView: View {
    let onSwipe: () -> Void

    @State private var offset: CGFloat = 0
    @State private var rotation: Double = 0
    @State private var shimmerOffset: CGFloat = -200
    @State private var borderOpacity: Double = 0.4
    @State private var breatheScale: CGFloat = 1.0

    var body: some View {
        HStack(spacing: PassSpacing.md) {
            Image(systemName: "sparkles")
                .font(.system(size: 24))
                .foregroundStyle(.white)

            VStack(alignment: .leading, spacing: PassSpacing.xs) {
                Text("Pro")
                    .font(.system(size: 22, weight: .bold, design: .rounded))
                    .foregroundStyle(.white)

                Text("スワイプで開く")
                    .font(.system(size: 13, weight: .medium))
                    .foregroundStyle(.white.opacity(0.7))
            }

            Spacer()

            Image(systemName: "chevron.right.2")
                .font(.system(size: 16, weight: .semibold))
                .foregroundStyle(.white.opacity(0.6))
        }
        .padding(PassSpacing.md)
        .background(
            ZStack {
                // Gradient background
                RoundedRectangle(cornerRadius: PassSpacing.cardCorner)
                    .fill(
                        LinearGradient(
                            colors: [PassColors.brand, PassColors.brandLight],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )

                // Shimmer streak
                RoundedRectangle(cornerRadius: PassSpacing.cardCorner)
                    .fill(Color.clear)
                    .overlay(
                        Rectangle()
                            .fill(
                                LinearGradient(
                                    colors: [
                                        .white.opacity(0),
                                        .white.opacity(0.25),
                                        .white.opacity(0),
                                    ],
                                    startPoint: .leading,
                                    endPoint: .trailing
                                )
                            )
                            .frame(width: 80)
                            .rotationEffect(.degrees(-20))
                            .offset(x: shimmerOffset)
                    )
                    .clipShape(RoundedRectangle(cornerRadius: PassSpacing.cardCorner))
            }
        )
        .overlay(
            RoundedRectangle(cornerRadius: PassSpacing.cardCorner)
                .stroke(
                    LinearGradient(
                        colors: [.white.opacity(borderOpacity), .white.opacity(borderOpacity * 0.5)],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ),
                    lineWidth: 1.5
                )
        )
        .scaleEffect(breatheScale)
        .offset(x: offset)
        .rotationEffect(.degrees(rotation))
        .gesture(
            DragGesture()
                .onChanged { value in
                    // Right-only: clamp negative direction
                    let translation = max(0, value.translation.width)
                    withAnimation(.interactiveSpring) {
                        offset = translation
                        rotation = Double(translation / 25)
                    }
                }
                .onEnded { value in
                    let threshold: CGFloat = 120
                    let translation = max(0, value.translation.width)
                    if translation > threshold {
                        PassHaptics.success()
                        withAnimation(.spring(response: 0.3, dampingFraction: 0.6)) {
                            offset = 500
                        }
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                            onSwipe()
                            offset = 0
                            rotation = 0
                        }
                    } else {
                        withAnimation(.spring(response: 0.4, dampingFraction: 0.7)) {
                            offset = 0
                            rotation = 0
                        }
                    }
                }
        )
        .onAppear {
            // Shimmer animation: sweep every 3 seconds
            withAnimation(
                .linear(duration: 1.5)
                .repeatForever(autoreverses: false)
                .delay(1.5)
            ) {
                shimmerOffset = 400
            }

            // Border pulse
            withAnimation(
                .easeInOut(duration: 1.5)
                .repeatForever(autoreverses: true)
            ) {
                borderOpacity = 0.8
            }

            // Breathe scale
            withAnimation(
                .easeInOut(duration: 2.0)
                .repeatForever(autoreverses: true)
            ) {
                breatheScale = 1.01
            }
        }
    }
}
