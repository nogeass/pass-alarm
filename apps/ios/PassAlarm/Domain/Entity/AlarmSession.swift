import Foundation

struct AlarmSession: Equatable, Sendable {
    let planId: UUID
    let totalRings: Int
    let intervalMin: Int
    var currentRingIndex: Int
    var isRinging: Bool
    var nextRingAt: Date?

    var isComplete: Bool {
        currentRingIndex >= totalRings
    }

    var progressText: String {
        "\(currentRingIndex)/\(totalRings)"
    }
}
