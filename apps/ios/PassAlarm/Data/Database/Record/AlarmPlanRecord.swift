import Foundation
import GRDB

struct AlarmPlanRecord: Codable, FetchableRecord, PersistableRecord, Sendable {
    static let databaseTableName = "alarm_plan"

    var id: String
    var isEnabled: Bool
    var label: String
    var timeHHmm: String
    var weekdaysMask: Int
    var repeatCount: Int
    var intervalMin: Int
    var soundId: String
    var holidayAutoSkip: Bool // kept in DB for backward compat, ignored in domain
    var createdAt: Date
    var updatedAt: Date

    init(from entity: AlarmPlan) {
        self.id = entity.id.uuidString
        self.isEnabled = entity.isEnabled
        self.label = entity.label
        self.timeHHmm = entity.timeHHmm
        self.weekdaysMask = Int(entity.weekdaysMask)
        self.repeatCount = entity.repeatCount
        self.intervalMin = entity.intervalMin
        self.soundId = entity.soundId
        self.holidayAutoSkip = true
        self.createdAt = entity.createdAt
        self.updatedAt = entity.updatedAt
    }

    func toEntity() -> AlarmPlan {
        AlarmPlan(
            id: UUID(uuidString: id) ?? UUID(),
            isEnabled: isEnabled,
            label: label,
            timeHHmm: timeHHmm,
            weekdaysMask: UInt8(weekdaysMask),
            repeatCount: repeatCount,
            intervalMin: intervalMin,
            soundId: soundId,
            createdAt: createdAt,
            updatedAt: updatedAt
        )
    }
}
