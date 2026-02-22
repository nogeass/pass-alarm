import SwiftUI

struct AlarmCardView: View {
    let occurrence: Occurrence
    var onSkip: () -> Void
    var onUnskip: () -> Void

    @State private var offset: CGFloat = 0
    @State private var rotation: Double = 0

    var body: some View {
        VStack(alignment: .leading, spacing: PassSpacing.sm) {
            HStack {
                VStack(alignment: .leading, spacing: PassSpacing.xs) {
                    Text(formatDate(occurrence.date))
                        .font(PassTypography.cardDate)
                        .foregroundStyle(.primary)

                    Text(occurrence.timeHHmm)
                        .font(PassTypography.cardTime)
                        .foregroundStyle(.primary)
                }

                Spacer()

                if occurrence.isSkipped {
                    // "Passed" badge
                    Text("パス済み")
                        .font(PassTypography.badgeText)
                        .foregroundStyle(.white)
                        .padding(.horizontal, PassSpacing.sm)
                        .padding(.vertical, PassSpacing.xs)
                        .background(Capsule().fill(PassColors.skipOrange))
                } else {
                    // Weekday badge
                    Text("平日")
                        .font(PassTypography.badgeText)
                        .foregroundStyle(.secondary)
                        .padding(.horizontal, PassSpacing.sm)
                        .padding(.vertical, PassSpacing.xs)
                        .background(Capsule().fill(Color.secondary.opacity(0.1)))
                }
            }

            if let reason = occurrence.skipReason {
                HStack(spacing: PassSpacing.xs) {
                    Image(systemName: reason == "祝日" ? "flag.fill" : "hand.raised.fill")
                        .font(.caption2)
                    Text(reason)
                        .font(.caption)
                }
                .foregroundStyle(PassColors.skipOrange)
            }
        }
        .padding(PassSpacing.md)
        .background(
            RoundedRectangle(cornerRadius: PassSpacing.cardCorner)
                .fill(occurrence.isSkipped
                      ? PassColors.cardBackgroundSkipped
                      : PassColors.cardBackground)
        )
        .overlay(
            RoundedRectangle(cornerRadius: PassSpacing.cardCorner)
                .stroke(Color.white.opacity(0.1), lineWidth: 1)
        )
        .opacity(occurrence.isSkipped ? 0.6 : 1.0)
        .offset(x: offset)
        .rotationEffect(.degrees(rotation))
        .gesture(
            DragGesture()
                .onChanged { value in
                    withAnimation(.interactiveSpring) {
                        offset = value.translation.width
                        rotation = Double(value.translation.width / 20)
                    }
                }
                .onEnded { value in
                    let threshold: CGFloat = 120
                    if value.translation.width > threshold && !occurrence.isSkipped {
                        PassHaptics.medium()
                        withAnimation(.spring(response: 0.3, dampingFraction: 0.6)) {
                            offset = 500
                        }
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                            onSkip()
                            offset = 0
                            rotation = 0
                        }
                    } else if value.translation.width < -threshold && occurrence.isSkipped {
                        PassHaptics.tap()
                        withAnimation(.spring(response: 0.3, dampingFraction: 0.6)) {
                            offset = -500
                        }
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                            onUnskip()
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
    }

    private func formatDate(_ dateStr: String) -> String {
        guard let date = Date.from(dateString: dateStr) else { return dateStr }
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ja_JP")
        formatter.dateFormat = "M/d (EEE)"
        return formatter.string(from: date)
    }
}
