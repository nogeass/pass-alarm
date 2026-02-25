import SwiftUI

struct GlobalSettingsSheet: View {
    @Environment(DIContainer.self) private var container
    @Environment(\.dismiss) private var dismiss
    @State private var holidayAutoSkip: Bool = true
    @State private var isPro: Bool = false
    @State private var isRestoring: Bool = false
    @State private var showProPurchase: Bool = false
    @State private var showToast = false
    @State private var toastMessage = ""
    @State private var showFeedback = false
    @State private var showRedeem = false
    @State private var proSource: ProSource = .store

    var body: some View {
        NavigationStack {
            ZStack {
                MapBackdrop(timeOfDay: .night)

                ScrollView {
                    VStack(spacing: PassSpacing.lg) {
                        // Holiday auto-skip
                        VStack(spacing: PassSpacing.sm) {
                            HStack {
                                VStack(alignment: .leading, spacing: PassSpacing.xs) {
                                    Text("祝日を自動スキップ")
                                        .foregroundStyle(.white)
                                    Text("日本の祝日は自動でパスします")
                                        .font(.caption)
                                        .foregroundStyle(.white.opacity(0.5))
                                }
                                Spacer()
                                Toggle("", isOn: $holidayAutoSkip)
                                    .labelsHidden()
                                    .tint(PassColors.brand)
                                    .onChange(of: holidayAutoSkip) { _, newValue in
                                        Task { await saveHolidaySetting(newValue) }
                                    }
                            }
                            .padding(PassSpacing.md)
                            .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: PassSpacing.cardCorner))
                        }

                        // Pro section
                        VStack(spacing: PassSpacing.md) {
                            Text("サブスクリプション")
                                .font(PassTypography.sectionHeader)
                                .foregroundStyle(.white.opacity(0.6))
                                .frame(maxWidth: .infinity, alignment: .leading)

                            if isPro {
                                HStack {
                                    Image(systemName: "sparkles")
                                        .foregroundStyle(PassColors.brandLight)
                                    Text("Pro メンバー")
                                        .foregroundStyle(.white)
                                    Spacer()
                                    Image(systemName: "checkmark.circle.fill")
                                        .foregroundStyle(PassColors.successGreen)
                                }
                                .padding(PassSpacing.md)
                                .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: PassSpacing.cardCorner))
                            } else {
                                PassButton(
                                    title: "Pro にアップグレード",
                                    size: .medium,
                                    color: PassColors.brand,
                                    haptic: .tap
                                ) {
                                    showProPurchase = true
                                }
                            }

                            Button {
                                Task { await restorePurchases() }
                            } label: {
                                HStack {
                                    Text("購入を復元")
                                        .font(.system(size: 15, weight: .medium))
                                        .foregroundStyle(.white.opacity(0.7))
                                    if isRestoring {
                                        ProgressView()
                                            .tint(.white)
                                            .scaleEffect(0.8)
                                    }
                                }
                            }
                            .disabled(isRestoring)
                        }

                        // Crowdfunding redeem
                        Button {
                            showRedeem = true
                        } label: {
                            HStack {
                                Image(systemName: "gift.fill")
                                    .foregroundStyle(PassColors.brandLight)
                                VStack(alignment: .leading, spacing: PassSpacing.xs) {
                                    if isPro && proSource == .crowdfund {
                                        Text("Pro 有効（ライフタイム）")
                                            .foregroundStyle(.white)
                                        Text("クラウドファンディング特典")
                                            .font(.caption)
                                            .foregroundStyle(.white.opacity(0.5))
                                    } else {
                                        Text("クラファン特典を受け取る")
                                            .foregroundStyle(.white)
                                        Text("支援者の方はこちら")
                                            .font(.caption)
                                            .foregroundStyle(.white.opacity(0.5))
                                    }
                                }
                                Spacer()
                                if isPro && proSource == .crowdfund {
                                    Image(systemName: "checkmark.circle.fill")
                                        .foregroundStyle(PassColors.successGreen)
                                } else {
                                    Image(systemName: "chevron.right")
                                        .foregroundStyle(.white.opacity(0.3))
                                }
                            }
                            .padding(PassSpacing.md)
                            .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: PassSpacing.cardCorner))
                        }

                        // Feedback
                        Button {
                            showFeedback = true
                        } label: {
                            HStack {
                                Image(systemName: "envelope")
                                    .foregroundStyle(.white.opacity(0.7))
                                VStack(alignment: .leading, spacing: PassSpacing.xs) {
                                    Text("アプリの改善要望を送信する")
                                        .foregroundStyle(.white)
                                    Text("ご意見をお聞かせください")
                                        .font(.caption)
                                        .foregroundStyle(.white.opacity(0.5))
                                }
                                Spacer()
                                Image(systemName: "chevron.right")
                                    .foregroundStyle(.white.opacity(0.3))
                            }
                            .padding(PassSpacing.md)
                            .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: PassSpacing.cardCorner))
                        }

                        Spacer()
                            .frame(height: PassSpacing.xxl)

                        // App version
                        Text("パスアラーム v\(appVersion)")
                            .font(.caption)
                            .foregroundStyle(.white.opacity(0.3))
                    }
                    .padding(PassSpacing.md)
                }

                VStack {
                    Spacer()
                    PraiseToast(message: toastMessage, isVisible: $showToast)
                        .padding(.bottom, PassSpacing.xl)
                }
            }
            .navigationTitle("設定")
            .navigationBarTitleDisplayMode(.inline)
            .toolbarBackground(.ultraThinMaterial, for: .navigationBar)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        PassHaptics.tap()
                        dismiss()
                    } label: {
                        Text("閉じる")
                            .foregroundStyle(.white)
                    }
                }
            }
        }
        .task {
            await loadSettings()
        }
        .sheet(isPresented: $showFeedback) {
            FeedbackSheet(
                onSent: {
                    showFeedback = false
                    toastMessage = "ご要望を送信しました！"
                    withAnimation(.spring(response: 0.3)) {
                        showToast = true
                    }
                }
            )
        }
        .sheet(isPresented: $showRedeem) {
            RedeemView()
                .environment(container)
        }
        .fullScreenCover(isPresented: $showProPurchase) {
            ProPurchaseView(
                onPurchased: {
                    showProPurchase = false
                    isPro = true
                    toastMessage = PraiseMessages.randomPurchase()
                    withAnimation(.spring(response: 0.3)) {
                        showToast = true
                    }
                },
                onDismiss: {
                    showProPurchase = false
                }
            )
        }
    }

    private var appVersion: String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0"
    }

    private func loadSettings() async {
        let settings = await container.appSettingsRepository.get()
        holidayAutoSkip = settings.holidayAutoSkip
        let status = await container.subscriptionRepository.currentStatus()
        isPro = status.isPro
        proSource = status.source
    }

    private func saveHolidaySetting(_ enabled: Bool) async {
        do {
            var settings = await container.appSettingsRepository.get()
            settings.holidayAutoSkip = enabled
            try await container.updateAppSettingsUseCase.execute(settings)
        } catch {
            print("Save holiday setting error: \(error)")
        }
    }

    private func restorePurchases() async {
        isRestoring = true
        defer { isRestoring = false }
        do {
            let status = try await container.subscriptionRepository.restorePurchases()
            isPro = status.isPro
            if status.isPro {
                toastMessage = PraiseMessages.randomPurchase()
                withAnimation(.spring(response: 0.3)) {
                    showToast = true
                }
            }
        } catch {
            print("Restore purchases error: \(error)")
        }
    }
}
