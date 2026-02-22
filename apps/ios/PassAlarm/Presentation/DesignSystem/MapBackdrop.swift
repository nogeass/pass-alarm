import SwiftUI

enum TimeOfDay {
    case morning, noon, evening, night

    var gradientColors: [Color] {
        switch self {
        case .morning: [PassColors.morningStart, PassColors.morningEnd]
        case .noon: [PassColors.noonStart, PassColors.noonEnd]
        case .evening: [PassColors.eveningStart, PassColors.eveningEnd]
        case .night: [PassColors.nightStart, PassColors.nightEnd]
        }
    }
}

struct MapBackdrop: View {
    let timeOfDay: TimeOfDay
    @State private var offset: CGFloat = 0

    var body: some View {
        GeometryReader { geo in
            ZStack {
                LinearGradient(
                    colors: timeOfDay.gradientColors,
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )

                // Subtle parallax circles (pin/island feel)
                Circle()
                    .fill(Color.white.opacity(0.03))
                    .frame(width: 300, height: 300)
                    .offset(x: geo.size.width * 0.3 + offset * 0.02,
                            y: geo.size.height * 0.2)

                Circle()
                    .fill(Color.white.opacity(0.05))
                    .frame(width: 200, height: 200)
                    .offset(x: -geo.size.width * 0.2 + offset * 0.015,
                            y: geo.size.height * 0.6)

                Circle()
                    .fill(Color.white.opacity(0.02))
                    .frame(width: 150, height: 150)
                    .offset(x: geo.size.width * 0.1 + offset * 0.01,
                            y: geo.size.height * 0.8)
            }
        }
        .ignoresSafeArea()
    }
}
