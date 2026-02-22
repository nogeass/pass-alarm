import Foundation
import GRDB

struct SkipExceptionRecord: Codable, FetchableRecord, PersistableRecord, Sendable {
    static let databaseTableName = "skip_exception"

    var id: String
    var date: String
    var reason: String
    var createdAt: Date

    init(from entity: SkipException) {
        self.id = entity.id.uuidString
        self.date = entity.date
        self.reason = entity.reason.rawValue
        self.createdAt = entity.createdAt
    }

    func toEntity() -> SkipException {
        SkipException(
            id: UUID(uuidString: id) ?? UUID(),
            date: date,
            reason: SkipException.SkipReason(rawValue: reason) ?? .manual,
            createdAt: createdAt
        )
    }
}
