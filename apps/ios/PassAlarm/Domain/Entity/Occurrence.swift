import Foundation

struct Occurrence: Identifiable, Equatable, Sendable {
    var id: String { date }
    var date: String // "YYYY-MM-DD"
    var timeHHmm: String
    var fireDate: Date
    var isSkipped: Bool
    var skipReason: String?
}
