import Foundation

final class ComputeQueueUseCase: Sendable {
    private let planRepository: AlarmPlanRepositoryProtocol
    private let skipRepository: SkipExceptionRepositoryProtocol
    private let holidayRepository: HolidayRepositoryProtocol
    private let appSettingsRepository: AppSettingsRepositoryProtocol

    static let lookaheadDays = 90

    init(planRepository: AlarmPlanRepositoryProtocol,
         skipRepository: SkipExceptionRepositoryProtocol,
         holidayRepository: HolidayRepositoryProtocol,
         appSettingsRepository: AppSettingsRepositoryProtocol) {
        self.planRepository = planRepository
        self.skipRepository = skipRepository
        self.holidayRepository = holidayRepository
        self.appSettingsRepository = appSettingsRepository
    }

    func execute(from now: Date = Date()) async throws -> [Occurrence] {
        let plans = try await planRepository.fetchEnabled()
        guard !plans.isEmpty else { return [] }

        let calendar = Calendar.current
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        formatter.locale = Locale(identifier: "en_US_POSIX")

        let startDate = calendar.startOfDay(for: now)
        let endDate = calendar.date(byAdding: .day, value: Self.lookaheadDays, to: startDate)!

        let fromStr = formatter.string(from: startDate)
        let toStr = formatter.string(from: endDate)

        let appSettings = await appSettingsRepository.get()

        let holidays = appSettings.holidayAutoSkip
            ? try await holidayRepository.fetchByDateRange(from: fromStr, to: toStr)
            : []
        let holidayDates = Set(holidays.map { $0.date })

        var allOccurrences: [Occurrence] = []

        for plan in plans {
            let skips = try await skipRepository.fetchByPlanAndDateRange(
                planId: plan.id, from: fromStr, to: toStr
            )
            let skipDates = Set(skips.map { $0.date })

            let weekdaySet = WeekdaySet(rawValue: plan.weekdaysMask)

            let timeParts = plan.timeHHmm.split(separator: ":").compactMap { Int($0) }
            guard timeParts.count == 2 else { continue }
            let hour = timeParts[0]
            let minute = timeParts[1]

            var currentDate = startDate

            while currentDate < endDate {
                let dateStr = formatter.string(from: currentDate)
                let calendarWeekday = calendar.component(.weekday, from: currentDate)

                if weekdaySet.containsCalendarWeekday(calendarWeekday) {
                    let isHolidaySkip = holidayDates.contains(dateStr)
                    let isManualSkip = skipDates.contains(dateStr)
                    let isSkipped = isHolidaySkip || isManualSkip

                    let fireDate = calendar.date(bySettingHour: hour, minute: minute, second: 0, of: currentDate)!
                    if fireDate <= now {
                        currentDate = calendar.date(byAdding: .day, value: 1, to: currentDate)!
                        continue
                    }

                    let skipReason: String? = isHolidaySkip ? "祝日" : (isManualSkip ? "手動スキップ" : nil)

                    allOccurrences.append(Occurrence(
                        planId: plan.id,
                        planLabel: plan.label,
                        date: dateStr,
                        timeHHmm: plan.timeHHmm,
                        soundId: plan.soundId,
                        fireDate: fireDate,
                        isSkipped: isSkipped,
                        skipReason: skipReason
                    ))
                }

                currentDate = calendar.date(byAdding: .day, value: 1, to: currentDate)!
            }
        }

        return allOccurrences.sorted { $0.fireDate < $1.fireDate }
    }
}
