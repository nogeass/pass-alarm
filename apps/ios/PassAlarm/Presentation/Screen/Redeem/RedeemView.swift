import SwiftUI

struct RedeemView: View {
    @Environment(DIContainer.self) private var container
    @Environment(\.dismiss) private var dismiss

    let initialToken: String?

    @State private var phase: RedeemPhase = .loading
    @State private var tokenText: String = ""
    @State private var authUser: AuthUser?
    @State private var errorMessage: String?

    init(token: String? = nil) {
        self.initialToken = token
    }

    var body: some View {
        NavigationStack {
            ZStack {
                MapBackdrop(timeOfDay: .night)

                ScrollView {
                    VStack(spacing: PassSpacing.lg) {
                        switch phase {
                        case .loading:
                            ProgressView()
                                .tint(.white)
                                .padding(.top, 80)

                        case .disabled:
                            statusCard(
                                icon: "pause.circle.fill",
                                iconColor: .orange,
                                title: "一時停止中",
                                message: "特典の受け取りは現在一時的に停止されています。しばらくしてからもう一度お試しください。"
                            )

                        case .needsAuth:
                            authSection

                        case .readyToClaim:
                            claimSection

                        case .claiming:
                            VStack(spacing: PassSpacing.md) {
                                ProgressView()
                                    .tint(.white)
                                Text("特典を有効化しています...")
                                    .foregroundStyle(.white.opacity(0.7))
                            }
                            .padding(.top, 80)

                        case .success:
                            statusCard(
                                icon: "checkmark.circle.fill",
                                iconColor: PassColors.successGreen,
                                title: "Pro有効化完了！",
                                message: "クラウドファンディング特典のPro版ライフタイムライセンスが有効になりました。"
                            )

                        case .error:
                            statusCard(
                                icon: "exclamationmark.triangle.fill",
                                iconColor: .red,
                                title: "エラー",
                                message: errorMessage ?? "予期しないエラーが発生しました"
                            )
                            retryButton
                        }
                    }
                    .padding(PassSpacing.md)
                    .padding(.top, PassSpacing.xl)
                }
            }
            .navigationTitle("クラファン特典")
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
            if let token = initialToken {
                tokenText = token
            }
            await checkConfig()
        }
    }

    // MARK: - Auth Section

    private var authSection: some View {
        VStack(spacing: PassSpacing.lg) {
            VStack(spacing: PassSpacing.sm) {
                Image(systemName: "gift.fill")
                    .font(.system(size: 48))
                    .foregroundStyle(PassColors.brandLight)

                Text("特典を受け取る")
                    .font(.title2.bold())
                    .foregroundStyle(.white)

                Text("サインインして特典をアクティベートしてください")
                    .font(.subheadline)
                    .foregroundStyle(.white.opacity(0.6))
                    .multilineTextAlignment(.center)
            }

            VStack(spacing: PassSpacing.md) {
                // Sign in with Apple
                Button {
                    Task { await signInWithApple() }
                } label: {
                    HStack {
                        Image(systemName: "apple.logo")
                        Text("Appleでサインイン")
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(.white)
                    .foregroundStyle(.black)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .font(.system(size: 16, weight: .semibold))
                }

                // Sign in with Google
                Button {
                    Task { await signInWithGoogle() }
                } label: {
                    HStack {
                        Image(systemName: "g.circle.fill")
                        Text("Googleでサインイン")
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(.white.opacity(0.15))
                    .foregroundStyle(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .font(.system(size: 16, weight: .semibold))
                }
            }
        }
    }

    // MARK: - Claim Section

    private var claimSection: some View {
        VStack(spacing: PassSpacing.lg) {
            VStack(spacing: PassSpacing.sm) {
                Image(systemName: "gift.fill")
                    .font(.system(size: 48))
                    .foregroundStyle(PassColors.brandLight)

                Text("特典を受け取る")
                    .font(.title2.bold())
                    .foregroundStyle(.white)

                if let email = authUser?.email {
                    Text(email)
                        .font(.caption)
                        .foregroundStyle(.white.opacity(0.5))
                }
            }

            if tokenText.isEmpty {
                // Manual token input
                VStack(spacing: PassSpacing.sm) {
                    Text("トークンを入力してください")
                        .font(.subheadline)
                        .foregroundStyle(.white.opacity(0.7))

                    TextField("", text: $tokenText, prompt: Text("例: ABcD1234efGh").foregroundStyle(.white.opacity(0.3)))
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                        .padding(PassSpacing.md)
                        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 12))
                        .foregroundStyle(.white)
                        .font(.system(size: 18, weight: .medium, design: .monospaced))
                }
            } else {
                // Token from deep link
                VStack(spacing: PassSpacing.xs) {
                    Text("トークン")
                        .font(.caption)
                        .foregroundStyle(.white.opacity(0.5))
                    Text(tokenText)
                        .font(.system(size: 20, weight: .bold, design: .monospaced))
                        .foregroundStyle(PassColors.brandLight)
                }
                .padding(PassSpacing.md)
                .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 12))
            }

            PassButton(
                title: "特典を受け取る",
                size: .medium,
                color: PassColors.brand,
                haptic: .tap
            ) {
                Task { await claim() }
            }
            .disabled(tokenText.trimmingCharacters(in: .whitespaces).isEmpty)
        }
    }

    // MARK: - Helper Views

    private func statusCard(icon: String, iconColor: Color, title: String, message: String) -> some View {
        VStack(spacing: PassSpacing.md) {
            Image(systemName: icon)
                .font(.system(size: 56))
                .foregroundStyle(iconColor)

            Text(title)
                .font(.title2.bold())
                .foregroundStyle(.white)

            Text(message)
                .font(.subheadline)
                .foregroundStyle(.white.opacity(0.7))
                .multilineTextAlignment(.center)
        }
        .padding(.top, 60)
    }

    private var retryButton: some View {
        Button {
            Task { await checkConfig() }
        } label: {
            Text("もう一度試す")
                .foregroundStyle(.white.opacity(0.7))
                .padding(.vertical, 10)
        }
    }

    // MARK: - Actions

    private func checkConfig() async {
        phase = .loading
        do {
            let disabled = try await container.serverEntitlementRepository.isRedeemDisabled()
            if disabled {
                phase = .disabled
                return
            }
        } catch {
            // Config fetch failed, proceed anyway
        }

        if container.authService.currentUser != nil {
            authUser = container.authService.currentUser
            phase = .readyToClaim
        } else {
            phase = .needsAuth
        }
    }

    private func signInWithApple() async {
        do {
            authUser = try await container.authService.signInWithApple()
            phase = .readyToClaim
        } catch {
            errorMessage = error.localizedDescription
            phase = .error
        }
    }

    private func signInWithGoogle() async {
        do {
            authUser = try await container.authService.signInWithGoogle()
            phase = .readyToClaim
        } catch {
            errorMessage = error.localizedDescription
            phase = .error
        }
    }

    private func claim() async {
        let token = tokenText.trimmingCharacters(in: .whitespaces)
        guard !token.isEmpty else { return }

        phase = .claiming
        do {
            _ = try await container.serverEntitlementRepository.claimToken(token)
            phase = .success
        } catch {
            errorMessage = error.localizedDescription
            phase = .error
        }
    }
}

// MARK: - Phase

private enum RedeemPhase {
    case loading
    case disabled
    case needsAuth
    case readyToClaim
    case claiming
    case success
    case error
}
