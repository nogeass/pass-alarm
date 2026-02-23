import Foundation

final class DeletePlanUseCase: Sendable {
    private let planRepository: AlarmPlanRepositoryProtocol
    private let reschedule: RescheduleNextNUseCase

    init(planRepository: AlarmPlanRepositoryProtocol,
         reschedule: RescheduleNextNUseCase) {
        self.planRepository = planRepository
        self.reschedule = reschedule
    }

    func execute(planId: UUID) async throws {
        try await planRepository.delete(planId)
        try await reschedule.execute()
    }
}
