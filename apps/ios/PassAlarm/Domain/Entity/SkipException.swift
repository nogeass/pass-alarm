import Foundation

struct SkipException: Identifiable, Equatable, Sendable, Codable {
    var id: UUID
    var date: String // "YYYY-MM-DD"
    var reason: SkipReason
    var createdAt: Date

    enum SkipReason: String, Codable, Sendable {
        case manual = "MANUAL"
        case holiday = "HOLIDAY"
        case system = "SYSTEM"
    }
}
