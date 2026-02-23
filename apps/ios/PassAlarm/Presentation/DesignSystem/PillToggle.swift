import SwiftUI

enum ContentTab: CaseIterable {
    case list, skip

    var label: String {
        switch self {
        case .list: "一覧"
        case .skip: "スキップ"
        }
    }
}

struct ModeToggleFAB: View {
    @Binding var selectedTab: ContentTab
    @State private var rotation: Double = 0

    private var iconName: String {
        switch selectedTab {
        case .list: "alarm.fill"
        case .skip: "calendar.badge.clock"
        }
    }

    private var fabColor: Color {
        switch selectedTab {
        case .list: PassColors.fabList
        case .skip: PassColors.fabSkip
        }
    }

    var body: some View {
        Button {
            PassHaptics.medium()
            withAnimation(.spring(response: 0.4, dampingFraction: 0.6)) {
                selectedTab = selectedTab == .list ? .skip : .list
                rotation += 360
            }
        } label: {
            Image(systemName: iconName)
                .font(.system(size: 30, weight: .heavy))
                .foregroundStyle(.white)
                .frame(width: 84, height: 84)
                .background(
                    Circle()
                        .fill(fabColor)
                )
                .clipShape(Circle())
                .shadow(color: .black.opacity(0.25), radius: 10, y: 4)
                .shadow(color: fabColor.opacity(0.4), radius: 16, y: 6)
                .rotationEffect(.degrees(rotation))
        }
        .buttonStyle(.plain)
    }
}
