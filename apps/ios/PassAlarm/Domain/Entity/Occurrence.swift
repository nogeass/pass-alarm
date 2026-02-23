import Foundation

struct Occurrence: Identifiable, Equatable, Sendable {
    var id: String { "\(planId.uuidString)-\(date)" }
    var planId: UUID
    var planLabel: String
    var date: String // "YYYY-MM-DD"
    var timeHHmm: String
    var soundId: String
    var fireDate: Date
    var isSkipped: Bool
    var skipReason: String?
}
