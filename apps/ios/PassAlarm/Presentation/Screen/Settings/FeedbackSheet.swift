import SwiftUI
import UIKit

struct FeedbackSheet: View {
    let onSent: () -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var message = ""
    @State private var isSending = false
    @State private var errorMessage: String?
    @FocusState private var isFocused: Bool

    private let maxLength = 2000

    var body: some View {
        NavigationStack {
            ZStack {
                MapBackdrop(timeOfDay: .night)

                VStack(spacing: PassSpacing.md) {
                    Text("どんな機能がほしいですか？\n不便なところはありますか？")
                        .font(.subheadline)
                        .foregroundStyle(.white.opacity(0.7))
                        .multilineTextAlignment(.center)
                        .padding(.top, PassSpacing.md)

                    TextEditor(text: $message)
                        .focused($isFocused)
                        .frame(minHeight: 150, maxHeight: 250)
                        .scrollContentBackground(.hidden)
                        .padding(PassSpacing.sm)
                        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: PassSpacing.cardCorner))
                        .foregroundStyle(.white)
                        .onChange(of: message) { _, newValue in
                            if newValue.count > maxLength {
                                message = String(newValue.prefix(maxLength))
                            }
                        }

                    HStack {
                        Text("\(message.count) / \(maxLength)")
                            .font(.caption)
                            .foregroundStyle(.white.opacity(0.3))
                        Spacer()
                    }

                    if let errorMessage {
                        Text(errorMessage)
                            .font(.caption)
                            .foregroundStyle(.red.opacity(0.8))
                    }

                    PassButton(
                        title: isSending ? "送信中…" : "送信する",
                        size: .medium,
                        color: PassColors.brand,
                        haptic: .tap
                    ) {
                        Task { await sendFeedback() }
                    }
                    .disabled(message.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || isSending)
                    .opacity(message.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? 0.5 : 1)

                    Spacer()
                }
                .padding(.horizontal, PassSpacing.md)
            }
            .navigationTitle("改善要望")
            .navigationBarTitleDisplayMode(.inline)
            .toolbarBackground(.ultraThinMaterial, for: .navigationBar)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        dismiss()
                    } label: {
                        Text("キャンセル")
                            .foregroundStyle(.white)
                    }
                }
            }
            .onAppear {
                isFocused = true
            }
        }
    }

    private func sendFeedback() async {
        isSending = true
        errorMessage = nil
        defer { isSending = false }

        do {
            try await FeedbackAPI.send(
                message: message,
                appVersion: Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "unknown",
                device: UIDevice.current.model,
                osVersion: "\(UIDevice.current.systemName) \(UIDevice.current.systemVersion)",
                platform: "iOS"
            )
            onSent()
        } catch {
            errorMessage = "送信に失敗しました。通信環境をご確認ください。"
        }
    }
}
