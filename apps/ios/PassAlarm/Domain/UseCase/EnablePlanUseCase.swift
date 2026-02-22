import Foundation

final class EnablePlanUseCase: Sendable {
    private let planRepository: AlarmPlanRepositoryProtocol
    private let reschedule: RescheduleNextNUseCase

    init(planRepository: AlarmPlanRepositoryProtocol,
         reschedule: RescheduleNextNUseCase) {
        self.planRepository = planRepository
        self.reschedule = reschedule
    }

    func execute(planId: UUID, isEnabled: Bool) async throws {
        guard var plan = try await planRepository.fetchById(planId) else { return }
        plan.isEnabled = isEnabled
        plan.updatedAt = Date()
        try await planRepository.save(plan)
        try await reschedule.execute()
    }
}
