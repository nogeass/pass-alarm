import SwiftUI

/// Visual content of an alarm card in the skip queue.
/// Swipe actions are handled by the parent List via `.swipeActions`.
struct AlarmCardView: View {
    let occurrence: Occurrence

    var body: some View {
        VStack(alignment: .leading, spacing: PassSpacing.sm) {
            HStack {
                VStack(alignment: .leading, spacing: PassSpacing.xs) {
                    if !occurrence.planLabel.isEmpty {
                        Text(occurrence.planLabel)
                            .font(PassTypography.badgeText)
                            .foregroundStyle(PassColors.brandLight)
                    }

                    Text(formatDate(occurrence.date))
                        .font(PassTypography.cardDate)
                        .foregroundStyle(.white)

                    Text(occurrence.timeHHmm)
                        .font(PassTypography.cardTime)
                        .foregroundStyle(.white)
                }

                Spacer()

                if occurrence.isSkipped {
                    Text("パス済み")
                        .font(PassTypography.badgeText)
                        .foregroundStyle(.white)
                        .padding(.horizontal, PassSpacing.sm)
                        .padding(.vertical, PassSpacing.xs)
                        .background(Capsule().fill(PassColors.skipOrange))
                } else {
                    Text(weekdayLabel(for: occurrence.date))
                        .font(PassTypography.badgeText)
                        .foregroundStyle(.white.opacity(0.7))
                        .padding(.horizontal, PassSpacing.sm)
                        .padding(.vertical, PassSpacing.xs)
                        .background(Capsule().fill(Color.white.opacity(0.15)))
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
    }

    private func formatDate(_ dateStr: String) -> String {
        guard let date = Date.from(dateString: dateStr) else { return dateStr }
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ja_JP")
        formatter.dateFormat = "M/d (EEE)"
        return formatter.string(from: date)
    }

    private func weekdayLabel(for dateStr: String) -> String {
        guard let date = Date.from(dateString: dateStr) else { return "" }
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ja_JP")
        formatter.dateFormat = "EEEE"
        return formatter.string(from: date)
    }
}
