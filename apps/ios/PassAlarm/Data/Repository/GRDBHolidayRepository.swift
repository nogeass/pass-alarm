import Foundation
import GRDB

final class GRDBHolidayRepository: HolidayRepositoryProtocol, Sendable {
    private let database: AppDatabase

    init(database: AppDatabase) {
        self.database = database
    }

    func fetchAll() async throws -> [HolidayJP] {
        try await database.dbQueue.read { db in
            try HolidayJPRecord.fetchAll(db).map { $0.toEntity() }
        }
    }

    func fetchByDateRange(from: String, to: String) async throws -> [HolidayJP] {
        try await database.dbQueue.read { db in
            try HolidayJPRecord
                .filter(Column("date") >= from && Column("date") <= to)
                .fetchAll(db)
                .map { $0.toEntity() }
        }
    }

    func isHoliday(_ date: String) async throws -> Bool {
        try await database.dbQueue.read { db in
            try HolidayJPRecord.fetchOne(db, key: date) != nil
        }
    }

    func insertAll(_ holidays: [HolidayJP]) async throws {
        try await database.dbQueue.write { db in
            for holiday in holidays {
                try HolidayJPRecord(from: holiday).save(db)
            }
        }
    }
}
