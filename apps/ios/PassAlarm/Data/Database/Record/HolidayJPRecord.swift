import Foundation
import GRDB

struct HolidayJPRecord: Codable, FetchableRecord, PersistableRecord, Sendable {
    static let databaseTableName = "holiday_jp"

    var date: String
    var nameJa: String

    init(from entity: HolidayJP) {
        self.date = entity.date
        self.nameJa = entity.nameJa
    }

    func toEntity() -> HolidayJP {
        HolidayJP(date: date, nameJa: nameJa)
    }
}
