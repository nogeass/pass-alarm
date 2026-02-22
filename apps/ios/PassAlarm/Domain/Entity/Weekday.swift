import Foundation

struct WeekdaySet: OptionSet, Sendable {
    let rawValue: UInt8

    static let monday    = WeekdaySet(rawValue: 1 << 0)
    static let tuesday   = WeekdaySet(rawValue: 1 << 1)
    static let wednesday = WeekdaySet(rawValue: 1 << 2)
    static let thursday  = WeekdaySet(rawValue: 1 << 3)
    static let friday    = WeekdaySet(rawValue: 1 << 4)
    static let saturday  = WeekdaySet(rawValue: 1 << 5)
    static let sunday    = WeekdaySet(rawValue: 1 << 6)

    static let weekdays: WeekdaySet = [.monday, .tuesday, .wednesday, .thursday, .friday]

    static let allDays: [(WeekdaySet, String, Int)] = [
        (.monday, "月", 2),
        (.tuesday, "火", 3),
        (.wednesday, "水", 4),
        (.thursday, "木", 5),
        (.friday, "金", 6),
        (.saturday, "土", 7),
        (.sunday, "日", 1),
    ]

    func containsCalendarWeekday(_ calendarWeekday: Int) -> Bool {
        for (day, _, cw) in WeekdaySet.allDays {
            if cw == calendarWeekday && self.contains(day) {
                return true
            }
        }
        return false
    }
}
