import SwiftUI

struct ProPurchaseView: View {
    let onPurchased: () -> Void
    let onDismiss: () -> Void

    @Environment(DIContainer.self) private var container
    @State private var products: [ProProduct] = []
    @State private var selectedPeriod: ProPeriod = .yearly
    @State private var isPurchasing = false
    @State private var appeared = false

    var body: some View {
        ZStack {
            MapBackdrop(timeOfDay: .evening)

            // Main content
            ScrollView {
                VStack(spacing: PassSpacing.lg) {
                    Spacer()
                        .frame(height: PassSpacing.xxl)

                    // Icon
                    Image(systemName: "sparkles")
                        .font(.system(size: 56))
                        .foregroundStyle(
                            LinearGradient(
                                colors: [.white, PassColors.brandLight],
                                startPoint: .top,
                                endPoint: .bottom
                            )
                        )

                    // Title
                    Text("Pass Pro")
                        .font(.system(size: 32, weight: .bold, design: .rounded))
                        .foregroundStyle(.white)

                    // Subtitle
                    Text("アラームを無限に設定しよう")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundStyle(.white.opacity(0.7))

                    // Feature rows
                    VStack(alignment: .leading, spacing: PassSpacing.md) {
                        featureRow(icon: "infinity", text: "アラームプラン無制限")
                        featureRow(icon: "star.fill", text: "すべての機能を利用可能")
                        featureRow(icon: "heart.fill", text: "開発をサポート")
                    }
                    .padding(.vertical, PassSpacing.md)

                    // Price pills
                    HStack(spacing: PassSpacing.md) {
                        if let monthly = products.first(where: { $0.period == .monthly }) {
                            PricePill(
                                periodLabel: "月額",
                                price: monthly.displayPrice,
                                pricePerMonth: nil,
                                trialText: monthly.trialText,
                                isYearly: false,
                                isSelected: selectedPeriod == .monthly
                            ) {
                                selectedPeriod = .monthly
                            }
                        }

                        if let yearly = products.first(where: { $0.period == .yearly }) {
                            PricePill(
                                periodLabel: "年額",
                                price: yearly.displayPrice,
                                pricePerMonth: yearly.pricePerMonth,
                                trialText: yearly.trialText,
                                isYearly: true,
                                isSelected: selectedPeriod == .yearly
                            ) {
                                selectedPeriod = .yearly
                            }
                        }
                    }
                    .padding(.horizontal, PassSpacing.md)

                    // Purchase button
                    PassButton(
                        title: isPurchasing ? "処理中..." : ctaTitle,
                        size: .large,
                        color: PassColors.brand,
                        isEnabled: !isPurchasing && !products.isEmpty,
                        haptic: .success
                    ) {
                        Task { await purchase() }
                    }
                    .padding(.horizontal, PassSpacing.md)

                    // Restore button
                    Button {
                        Task { await restore() }
                    } label: {
                        Text("復元する")
                            .font(.caption)
                            .foregroundStyle(.white.opacity(0.5))
                    }
                    .padding(.bottom, PassSpacing.xl)
                }
                .padding(.horizontal, PassSpacing.md)
            }

            // Close button (on top of ScrollView)
            VStack {
                HStack {
                    Spacer()
                    Button {
                        PassHaptics.tap()
                        onDismiss()
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 28))
                            .foregroundStyle(.white.opacity(0.6))
                    }
                }
                .padding(.horizontal, PassSpacing.lg)
                .padding(.top, PassSpacing.md)

                Spacer()
            }
        }
        .scaleEffect(appeared ? 1.0 : 0.8)
        .opacity(appeared ? 1.0 : 0.0)
        .onAppear {
            withAnimation(.spring(response: 0.5, dampingFraction: 0.8)) {
                appeared = true
            }
        }
        .task {
            await loadProducts()
        }
    }

    // MARK: - Feature Row

    private func featureRow(icon: String, text: String) -> some View {
        HStack(spacing: PassSpacing.md) {
            Image(systemName: icon)
                .font(.system(size: 18))
                .foregroundStyle(PassColors.brandLight)
                .frame(width: 28)

            Text(text)
                .font(.system(size: 16, weight: .medium))
                .foregroundStyle(.white)
        }
    }

    private var ctaTitle: String {
        let selected = products.first(where: { $0.period == selectedPeriod })
        if let trial = selected?.trialText {
            return "\(trial)で試す"
        }
        return "はじめる"
    }

    // MARK: - Actions

    private func loadProducts() async {
        do {
            products = try await container.subscriptionRepository.fetchProducts()
        } catch {
            print("ProPurchaseView loadProducts error: \(error)")
        }
    }

    private func purchase() async {
        guard let product = products.first(where: { $0.period == selectedPeriod }) else { return }
        isPurchasing = true
        defer { isPurchasing = false }

        do {
            let status = try await container.subscriptionRepository.purchase(product)
            if status.isPro {
                onPurchased()
            }
        } catch {
            print("ProPurchaseView purchase error: \(error)")
        }
    }

    private func restore() async {
        isPurchasing = true
        defer { isPurchasing = false }

        do {
            let status = try await container.subscriptionRepository.restorePurchases()
            if status.isPro {
                onPurchased()
            }
        } catch {
            print("ProPurchaseView restore error: \(error)")
        }
    }
}
