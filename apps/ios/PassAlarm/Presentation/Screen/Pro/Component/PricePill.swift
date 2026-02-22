import SwiftUI

struct PricePill: View {
    let periodLabel: String
    let price: String
    let pricePerMonth: String?
    let isYearly: Bool
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        VStack(spacing: PassSpacing.sm) {
            // Period label
            Text(periodLabel)
                .font(.system(size: 13, weight: .medium))
                .foregroundStyle(isSelected ? .white.opacity(0.8) : .white.opacity(0.5))

            // Price
            Text(price)
                .font(.system(size: 20, weight: .bold, design: .rounded))
                .foregroundStyle(.white)

            // Price per month (yearly only)
            if let perMonth = pricePerMonth {
                Text(perMonth)
                    .font(.system(size: 11, weight: .medium))
                    .foregroundStyle(.white.opacity(0.6))
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, PassSpacing.md)
        .padding(.horizontal, PassSpacing.sm)
        .background(
            RoundedRectangle(cornerRadius: PassSpacing.cardCorner)
                .fill(isSelected ? PassColors.brand : Color.white.opacity(0.1))
        )
        .overlay(
            RoundedRectangle(cornerRadius: PassSpacing.cardCorner)
                .stroke(isSelected ? Color.white.opacity(0.5) : Color.clear, lineWidth: 1.5)
        )
        .overlay(alignment: .top) {
            if isYearly {
                Text("おトク")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundStyle(.white)
                    .padding(.horizontal, PassSpacing.sm)
                    .padding(.vertical, PassSpacing.xs)
                    .background(Capsule().fill(PassColors.successGreen))
                    .offset(y: -12)
            }
        }
        .contentShape(Rectangle())
        .onTapGesture {
            PassHaptics.tap()
            onTap()
        }
    }
}
