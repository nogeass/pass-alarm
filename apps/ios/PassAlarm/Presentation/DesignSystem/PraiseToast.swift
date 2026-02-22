import SwiftUI

struct PraiseToast: View {
    let message: String
    @Binding var isVisible: Bool

    var body: some View {
        if isVisible {
            Text(message)
                .font(PassTypography.toastText)
                .foregroundStyle(.white)
                .padding(.horizontal, PassSpacing.lg)
                .padding(.vertical, PassSpacing.md)
                .background(
                    Capsule()
                        .fill(.ultraThinMaterial)
                        .background(Capsule().fill(Color.black.opacity(0.7)))
                )
                .transition(.move(edge: .bottom).combined(with: .opacity))
                .onAppear {
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
                        withAnimation(.easeOut(duration: 0.3)) {
                            isVisible = false
                        }
                    }
                }
        }
    }
}

enum PraiseMessages {
    // Wake up (80% chance to show)
    static let wakeUp = [
        "起きた。えらすぎ。",
        "今日も勝ち",
        "天才かもしれない",
        "偉業を達成した",
        "朝を制した",
        "ナイス起床",
        "今日のヒーローはあなた",
        "起きた、天才",
    ]

    // Skip (50% chance to show)
    static let skip = [
        "今日はパスでOK",
        "無理しないのが正解",
        "パスした",
        "いいね、休もう",
        "今日はゆっくり",
    ]

    // Settings complete
    static let settingsComplete = [
        "準備OK。任せて",
        "セット完了",
        "いい感じ",
        "明日から起こすね",
    ]

    // Purchase
    static let purchase = [
        "最高。これで無限。",
        "ありがとう",
        "アップグレード完了",
    ]

    static func randomWakeUp() -> String? {
        Double.random(in: 0...1) < 0.8 ? wakeUp.randomElement() : nil
    }

    static func randomSkip() -> String? {
        Double.random(in: 0...1) < 0.5 ? skip.randomElement() : nil
    }

    static func randomSettingsComplete() -> String {
        settingsComplete.randomElement() ?? "OK"
    }

    static func randomPurchase() -> String {
        purchase.randomElement() ?? "OK"
    }
}
