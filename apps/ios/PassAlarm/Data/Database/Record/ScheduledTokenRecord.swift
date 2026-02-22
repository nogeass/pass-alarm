import Foundation
import GRDB

struct ScheduledTokenRecord: Codable, FetchableRecord, PersistableRecord, Sendable {
    static let databaseTableName = "scheduled_token"

    var id: String
    var date: String
    var fireAtEpoch: Double
    var osIdentifier: String
    var status: String
    var createdAt: Date
    var updatedAt: Date

    init(from entity: ScheduledToken) {
        self.id = entity.id.uuidString
        self.date = entity.date
        self.fireAtEpoch = entity.fireAtEpoch
        self.osIdentifier = entity.osIdentifier
        self.status = entity.status.rawValue
        self.createdAt = entity.createdAt
        self.updatedAt = entity.updatedAt
    }

    func toEntity() -> ScheduledToken {
        ScheduledToken(
            id: UUID(uuidString: id) ?? UUID(),
            date: date,
            fireAtEpoch: fireAtEpoch,
            osIdentifier: osIdentifier,
            status: TokenStatus(rawValue: status) ?? .scheduled,
            createdAt: createdAt,
            updatedAt: updatedAt
        )
    }
}
