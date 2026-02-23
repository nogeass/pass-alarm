import Foundation

struct ScheduledToken: Identifiable, Equatable, Sendable, Codable {
    var id: UUID
    var planId: UUID
    var date: String // "YYYY-MM-DD"
    var fireAtEpoch: TimeInterval
    var osIdentifier: String
    var status: TokenStatus
    var createdAt: Date
    var updatedAt: Date
}
