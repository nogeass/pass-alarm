import Foundation

final class SeedDefaultAlarmsUseCase: Sendable {
    private let planRepository: AlarmPlanRepositoryProtocol
    private let reschedule: RescheduleNextNUseCase

    init(planRepository: AlarmPlanRepositoryProtocol,
         reschedule: RescheduleNextNUseCase) {
        self.planRepository = planRepository
        self.reschedule = reschedule
    }

    func execute() async throws {
        let existing = try await planRepository.fetchAll()
        guard existing.isEmpty else { return }

        let now = Date()

        let weekdayMask: UInt8 = WeekdaySet.weekdays.rawValue

        let times = ["06:00", "07:00", "08:00", "09:00", "10:00"]

        for time in times {
            let plan = AlarmPlan(
                id: UUID(),
                isEnabled: false,
                label: "",
                timeHHmm: time,
                weekdaysMask: weekdayMask,
                repeatCount: 10,
                intervalMin: 5,
                soundId: "default",
                createdAt: now,
                updatedAt: now
            )
            try await planRepository.save(plan)
        }
    }
}
