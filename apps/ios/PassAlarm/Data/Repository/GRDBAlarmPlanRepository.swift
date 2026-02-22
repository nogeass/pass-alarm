import Foundation
import GRDB

final class GRDBAlarmPlanRepository: AlarmPlanRepositoryProtocol, Sendable {
    private let database: AppDatabase

    init(database: AppDatabase) {
        self.database = database
    }

    func fetchAll() async throws -> [AlarmPlan] {
        try await database.dbQueue.read { db in
            try AlarmPlanRecord.fetchAll(db).map { $0.toEntity() }
        }
    }

    func fetchById(_ id: UUID) async throws -> AlarmPlan? {
        try await database.dbQueue.read { db in
            try AlarmPlanRecord.fetchOne(db, key: id.uuidString)?.toEntity()
        }
    }

    func save(_ plan: AlarmPlan) async throws {
        try await database.dbQueue.write { db in
            try AlarmPlanRecord(from: plan).save(db)
        }
    }

    func delete(_ id: UUID) async throws {
        try await database.dbQueue.write { db in
            _ = try AlarmPlanRecord.deleteOne(db, key: id.uuidString)
        }
    }
}
