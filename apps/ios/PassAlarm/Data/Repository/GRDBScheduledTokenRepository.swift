import Foundation
import GRDB

final class GRDBScheduledTokenRepository: ScheduledTokenRepositoryProtocol, Sendable {
    private let database: AppDatabase

    init(database: AppDatabase) {
        self.database = database
    }

    func fetchAll() async throws -> [ScheduledToken] {
        try await database.dbQueue.read { db in
            try ScheduledTokenRecord.fetchAll(db).map { $0.toEntity() }
        }
    }

    func fetchScheduled() async throws -> [ScheduledToken] {
        try await database.dbQueue.read { db in
            try ScheduledTokenRecord
                .filter(Column("status") == TokenStatus.scheduled.rawValue)
                .fetchAll(db)
                .map { $0.toEntity() }
        }
    }

    func fetchByOsIdentifier(_ osId: String) async throws -> ScheduledToken? {
        try await database.dbQueue.read { db in
            try ScheduledTokenRecord
                .filter(Column("osIdentifier") == osId)
                .fetchOne(db)?
                .toEntity()
        }
    }

    func save(_ token: ScheduledToken) async throws {
        try await database.dbQueue.write { db in
            try ScheduledTokenRecord(from: token).save(db)
        }
    }

    func saveAll(_ tokens: [ScheduledToken]) async throws {
        try await database.dbQueue.write { db in
            for token in tokens {
                try ScheduledTokenRecord(from: token).save(db)
            }
        }
    }

    func updateStatus(_ id: UUID, status: TokenStatus) async throws {
        try await database.dbQueue.write { db in
            if var record = try ScheduledTokenRecord.fetchOne(db, key: id.uuidString) {
                record.status = status.rawValue
                record.updatedAt = Date()
                try record.update(db)
            }
        }
    }

    func deleteAll() async throws {
        try await database.dbQueue.write { db in
            _ = try ScheduledTokenRecord.deleteAll(db)
        }
    }

    func deleteScheduled() async throws {
        try await database.dbQueue.write { db in
            _ = try ScheduledTokenRecord
                .filter(Column("status") == TokenStatus.scheduled.rawValue)
                .deleteAll(db)
        }
    }
}
