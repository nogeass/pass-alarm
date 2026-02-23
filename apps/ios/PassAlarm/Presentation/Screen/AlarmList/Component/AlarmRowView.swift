import SwiftUI

struct AlarmRowView: View {
    let plan: AlarmPlan
    var onToggle: (Bool) -> Void
    var onTap: () -> Void

    @State private var isEnabled: Bool

    init(plan: AlarmPlan, onToggle: @escaping (Bool) -> Void, onTap: @escaping () -> Void) {
        self.plan = plan
        self.onToggle = onToggle
        self.onTap = onTap
        self._isEnabled = State(initialValue: plan.isEnabled)
    }

    var body: some View {
        Button {
            PassHaptics.tap()
            onTap()
        } label: {
            HStack(alignment: .center, spacing: PassSpacing.md) {
                VStack(alignment: .leading, spacing: PassSpacing.xs) {
                    Text(plan.timeHHmm)
                        .font(PassTypography.cardTime)
                        .foregroundStyle(isEnabled ? .white : .white.opacity(0.4))

                    HStack(spacing: PassSpacing.xs) {
                        Text(weekdayDescription)
                            .font(PassTypography.badgeText)
                            .foregroundStyle(isEnabled ? .white.opacity(0.6) : .white.opacity(0.3))

                        if !plan.label.isEmpty {
                            Text(plan.label)
                                .font(PassTypography.badgeText)
                                .foregroundStyle(isEnabled ? PassColors.brandLight : .white.opacity(0.3))
                        }
                    }
                }

                Spacer()

                Toggle("", isOn: $isEnabled)
                    .labelsHidden()
                    .tint(PassColors.brand)
                    .onChange(of: isEnabled) { _, newValue in
                        PassHaptics.medium()
                        onToggle(newValue)
                    }
            }
            .padding(PassSpacing.md)
            .background(
                RoundedRectangle(cornerRadius: PassSpacing.cardCorner)
                    .fill(PassColors.cardBackground)
            )
            .overlay(
                RoundedRectangle(cornerRadius: PassSpacing.cardCorner)
                    .stroke(Color.white.opacity(0.1), lineWidth: 1)
            )
            .opacity(isEnabled ? 1.0 : 0.6)
        }
        .buttonStyle(.plain)
    }

    private var weekdayDescription: String {
        let set = WeekdaySet(rawValue: plan.weekdaysMask)
        let allMask: UInt8 = 0b01111111
        let weekdaysMask = WeekdaySet.weekdays.rawValue

        if plan.weekdaysMask == allMask {
            return "毎日"
        }
        if plan.weekdaysMask == weekdaysMask {
            return "平日"
        }

        let weekendMask: UInt8 = WeekdaySet.saturday.rawValue | WeekdaySet.sunday.rawValue
        if plan.weekdaysMask == weekendMask {
            return "週末"
        }

        var labels: [String] = []
        for (day, label, _) in WeekdaySet.allDays {
            if set.contains(day) {
                labels.append(label)
            }
        }
        return labels.joined()
    }
}
