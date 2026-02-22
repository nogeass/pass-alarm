import Foundation
import GRDB

struct AlarmPlanRecord: Codable, FetchableRecord, PersistableRecord, Sendable {
    static let databaseTableName = "alarm_plan"

    var id: String
    var isEnabled: Bool
    var timeHHmm: String
    var weekdaysMask: Int
    var repeatCount: Int
    var intervalMin: Int
    var holidayAutoSkip: Bool
    var createdAt: Date
    var updatedAt: Date

    init(from entity: AlarmPlan) {
        self.id = entity.id.uuidString
        self.isEnabled = entity.isEnabled
        self.timeHHmm = entity.timeHHmm
        self.weekdaysMask = Int(entity.weekdaysMask)
        self.repeatCount = entity.repeatCount
        self.intervalMin = entity.intervalMin
        self.holidayAutoSkip = entity.holidayAutoSkip
        self.createdAt = entity.createdAt
        self.updatedAt = entity.updatedAt
    }

    func toEntity() -> AlarmPlan {
        AlarmPlan(
            id: UUID(uuidString: id) ?? UUID(),
            isEnabled: isEnabled,
            timeHHmm: timeHHmm,
            weekdaysMask: UInt8(weekdaysMask),
            repeatCount: repeatCount,
            intervalMin: intervalMin,
            holidayAutoSkip: holidayAutoSkip,
            createdAt: createdAt,
            updatedAt: updatedAt
        )
    }
}
