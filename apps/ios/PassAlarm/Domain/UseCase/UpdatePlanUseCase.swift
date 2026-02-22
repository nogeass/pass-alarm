import Foundation

final class UpdatePlanUseCase: Sendable {
    private let planRepository: AlarmPlanRepositoryProtocol
    private let reschedule: RescheduleNextNUseCase

    init(planRepository: AlarmPlanRepositoryProtocol,
         reschedule: RescheduleNextNUseCase) {
        self.planRepository = planRepository
        self.reschedule = reschedule
    }

    func execute(_ plan: AlarmPlan) async throws {
        var updated = plan
        updated.updatedAt = Date()
        try await planRepository.save(updated)
        try await reschedule.execute()
    }
}
