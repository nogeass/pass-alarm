import Foundation

struct AlarmPlan: Identifiable, Equatable, Sendable, Codable {
    var id: UUID
    var isEnabled: Bool
    var label: String
    var timeHHmm: String // "07:00"
    var weekdaysMask: UInt8 // Mon=1, Tue=2, Wed=4, Thu=8, Fri=16, Sat=32, Sun=64
    var repeatCount: Int
    var intervalMin: Int
    var soundId: String
    var createdAt: Date
    var updatedAt: Date

    static let `default` = AlarmPlan(
        id: UUID(),
        isEnabled: true,
        label: "",
        timeHHmm: "07:00",
        weekdaysMask: 0b00011111, // Mon-Fri
        repeatCount: 10,
        intervalMin: 5,
        soundId: "default",
        createdAt: Date(),
        updatedAt: Date()
    )
}
