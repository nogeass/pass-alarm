import SwiftUI

enum PassColors {
    // Time-of-day gradients
    static let morningStart = Color(red: 1.0, green: 0.82, blue: 0.55) // warm yellow
    static let morningEnd = Color(red: 1.0, green: 0.62, blue: 0.45) // soft orange
    static let noonStart = Color(red: 0.53, green: 0.81, blue: 1.0) // sky blue
    static let noonEnd = Color(red: 0.40, green: 0.68, blue: 0.95) // deeper blue
    static let eveningStart = Color(red: 0.96, green: 0.56, blue: 0.45) // sunset
    static let eveningEnd = Color(red: 0.65, green: 0.35, blue: 0.68) // purple
    static let nightStart = Color(red: 0.10, green: 0.10, blue: 0.22) // deep navy
    static let nightEnd = Color(red: 0.18, green: 0.15, blue: 0.30) // dark purple

    // Brand
    static let brand = Color(red: 0.39, green: 0.40, blue: 0.95) // indigo
    static let brandLight = Color(red: 0.55, green: 0.56, blue: 1.0)

    // FAB toggle (Zenly-inspired neon)
    static let fabList = Color(red: 1.0, green: 0.42, blue: 0.21)    // neon orange #FF6B35
    static let fabSkip = Color(red: 0.29, green: 0.62, blue: 0.96)   // electric blue #4A9FF5

    // Semantic
    static let skipOrange = Color(red: 0.96, green: 0.62, blue: 0.04)
    static let stopRed = Color(red: 0.93, green: 0.26, blue: 0.26)
    static let snoozeAmber = Color(red: 0.96, green: 0.62, blue: 0.04)
    static let successGreen = Color(red: 0.22, green: 0.80, blue: 0.47)

    // Surface
    static let cardBackground = Color.white.opacity(0.12)
    static let cardBackgroundSkipped = Color.orange.opacity(0.15)
}
