import Foundation
import GRDB

final class GRDBSkipExceptionRepository: SkipExceptionRepositoryProtocol, Sendable {
    private let database: AppDatabase

    init(database: AppDatabase) {
        self.database = database
    }

    func fetchAll() async throws -> [SkipException] {
        try await database.dbQueue.read { db in
            try SkipExceptionRecord.fetchAll(db).map { $0.toEntity() }
        }
    }

    func fetchByDateRange(from: String, to: String) async throws -> [SkipException] {
        try await database.dbQueue.read { db in
            try SkipExceptionRecord
                .filter(Column("date") >= from && Column("date") <= to)
                .fetchAll(db)
                .map { $0.toEntity() }
        }
    }

    func save(_ skip: SkipException) async throws {
        try await database.dbQueue.write { db in
            try SkipExceptionRecord(from: skip).save(db)
        }
    }

    func delete(_ id: UUID) async throws {
        try await database.dbQueue.write { db in
            _ = try SkipExceptionRecord.deleteOne(db, key: id.uuidString)
        }
    }

    func deleteByDate(_ date: String) async throws {
        try await database.dbQueue.write { db in
            _ = try SkipExceptionRecord.filter(Column("date") == date).deleteAll(db)
        }
    }

    func fetchByPlanAndDateRange(planId: UUID, from: String, to: String) async throws -> [SkipException] {
        try await database.dbQueue.read { db in
            try SkipExceptionRecord
                .filter(Column("planId") == planId.uuidString && Column("date") >= from && Column("date") <= to)
                .fetchAll(db)
                .map { $0.toEntity() }
        }
    }

    func deleteByPlanAndDate(planId: UUID, date: String) async throws {
        try await database.dbQueue.write { db in
            _ = try SkipExceptionRecord
                .filter(Column("planId") == planId.uuidString && Column("date") == date)
                .deleteAll(db)
        }
    }
}
