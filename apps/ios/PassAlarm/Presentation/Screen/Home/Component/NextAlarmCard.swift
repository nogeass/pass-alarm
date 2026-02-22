import SwiftUI

struct NextAlarmCard: View {
    let occurrence: Occurrence

    var body: some View {
        VStack(spacing: PassSpacing.sm) {
            Text("次のアラーム")
                .font(PassTypography.sectionHeader)
                .foregroundStyle(.white.opacity(0.6))
                .textCase(.uppercase)

            Text(occurrence.timeHHmm)
                .font(PassTypography.heroTime)
                .foregroundStyle(.white)

            Text(formatDate(occurrence.date))
                .font(PassTypography.cardDate)
                .foregroundStyle(.white.opacity(0.7))

            if let reason = occurrence.skipReason {
                HStack(spacing: PassSpacing.xs) {
                    Circle()
                        .fill(PassColors.skipOrange)
                        .frame(width: 6, height: 6)
                    Text(reason)
                        .font(PassTypography.badgeText)
                        .foregroundStyle(PassColors.skipOrange)
                }
                .padding(.horizontal, PassSpacing.sm)
                .padding(.vertical, PassSpacing.xs)
                .background(
                    Capsule().fill(PassColors.skipOrange.opacity(0.15))
                )
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, PassSpacing.xl)
        .padding(.horizontal, PassSpacing.lg)
        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: PassSpacing.cardCorner))
    }

    private func formatDate(_ dateStr: String) -> String {
        guard let date = Date.from(dateString: dateStr) else { return dateStr }
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ja_JP")
        formatter.dateFormat = "M/d (EEE)"
        return formatter.string(from: date)
    }
}
