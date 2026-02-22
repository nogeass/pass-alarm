import Foundation
import GRDB

final class AppDatabase: Sendable {
    let dbQueue: DatabaseQueue

    static let shared: AppDatabase = {
        let path = AppDatabase.databasePath()
        do {
            let dbQueue = try DatabaseQueue(path: path)
            let db = AppDatabase(dbQueue: dbQueue)
            try db.migrate()
            return db
        } catch {
            fatalError("Database initialization failed: \(error)")
        }
    }()

    init(dbQueue: DatabaseQueue) {
        self.dbQueue = dbQueue
    }

    static func empty() throws -> AppDatabase {
        let dbQueue = try DatabaseQueue(configuration: .init())
        let db = AppDatabase(dbQueue: dbQueue)
        try db.migrate()
        return db
    }

    private static func databasePath() -> String {
        let fileManager = FileManager.default
        let appSupport = fileManager.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!
        let dir = appSupport.appendingPathComponent("PassAlarm", isDirectory: true)
        try? fileManager.createDirectory(at: dir, withIntermediateDirectories: true)
        return dir.appendingPathComponent("passalarm.sqlite").path
    }

    private func migrate() throws {
        var migrator = DatabaseMigrator()

        migrator.registerMigration("v1") { db in
            try db.create(table: "alarm_plan") { t in
                t.column("id", .text).primaryKey()
                t.column("isEnabled", .boolean).notNull().defaults(to: true)
                t.column("timeHHmm", .text).notNull().defaults(to: "07:00")
                t.column("weekdaysMask", .integer).notNull().defaults(to: 0b00011111)
                t.column("repeatCount", .integer).notNull().defaults(to: 10)
                t.column("intervalMin", .integer).notNull().defaults(to: 5)
                t.column("holidayAutoSkip", .boolean).notNull().defaults(to: true)
                t.column("createdAt", .datetime).notNull()
                t.column("updatedAt", .datetime).notNull()
            }

            try db.create(table: "skip_exception") { t in
                t.column("id", .text).primaryKey()
                t.column("date", .text).notNull()
                t.column("reason", .text).notNull()
                t.column("createdAt", .datetime).notNull()
            }
            try db.create(index: "idx_skip_exception_date", on: "skip_exception", columns: ["date"])

            try db.create(table: "scheduled_token") { t in
                t.column("id", .text).primaryKey()
                t.column("date", .text).notNull()
                t.column("fireAtEpoch", .double).notNull()
                t.column("osIdentifier", .text).notNull()
                t.column("status", .text).notNull()
                t.column("createdAt", .datetime).notNull()
                t.column("updatedAt", .datetime).notNull()
            }
            try db.create(index: "idx_scheduled_token_status", on: "scheduled_token", columns: ["status"])
            try db.create(index: "idx_scheduled_token_osId", on: "scheduled_token", columns: ["osIdentifier"])

            try db.create(table: "holiday_jp") { t in
                t.column("date", .text).primaryKey()
                t.column("nameJa", .text).notNull()
            }
        }

        try migrator.migrate(dbQueue)
    }
}
