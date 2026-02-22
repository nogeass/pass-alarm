import Foundation

@Observable
final class AlarmSettingsViewModel {
    var selectedTime: Date = Date()
    var weekdaysMask: UInt8 = 0b00011111
    var repeatCount: Int = 10
    var intervalMin: Int = 5
    var holidayAutoSkip: Bool = true

    private var plan: AlarmPlan?
    private let container: DIContainer

    init(container: DIContainer) {
        self.container = container
    }

    func load() async {
        do {
            let plans = try await container.alarmPlanRepository.fetchAll()
            if let existing = plans.first {
                plan = existing
                weekdaysMask = existing.weekdaysMask
                repeatCount = existing.repeatCount
                intervalMin = existing.intervalMin
                holidayAutoSkip = existing.holidayAutoSkip

                let parts = existing.timeHHmm.split(separator: ":").compactMap { Int($0) }
                if parts.count == 2 {
                    let calendar = Calendar.current
                    selectedTime = calendar.date(bySettingHour: parts[0], minute: parts[1], second: 0, of: Date()) ?? Date()
                }
            }
        } catch {
            print("Load settings error: \(error)")
        }
    }

    func save() async {
        let calendar = Calendar.current
        let hour = calendar.component(.hour, from: selectedTime)
        let minute = calendar.component(.minute, from: selectedTime)
        let timeStr = String(format: "%02d:%02d", hour, minute)

        var updated = plan ?? AlarmPlan.default
        updated.timeHHmm = timeStr
        updated.weekdaysMask = weekdaysMask
        updated.repeatCount = repeatCount
        updated.intervalMin = intervalMin
        updated.holidayAutoSkip = holidayAutoSkip
        updated.updatedAt = Date()

        do {
            try await container.updatePlanUseCase.execute(updated)
            plan = updated
        } catch {
            print("Save error: \(error)")
        }
    }
}
