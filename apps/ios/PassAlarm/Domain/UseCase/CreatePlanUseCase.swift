import Foundation

final class CreatePlanUseCase: Sendable {
    private let planRepository: AlarmPlanRepositoryProtocol
    private let checkProLimit: CheckProLimitUseCase
    private let reschedule: RescheduleNextNUseCase

    init(planRepository: AlarmPlanRepositoryProtocol,
         checkProLimit: CheckProLimitUseCase,
         reschedule: RescheduleNextNUseCase) {
        self.planRepository = planRepository
        self.checkProLimit = checkProLimit
        self.reschedule = reschedule
    }

    enum CreatePlanError: Error {
        case limitReached
    }

    func execute(_ plan: AlarmPlan) async throws {
        let canCreate = try await checkProLimit.execute()
        guard canCreate else { throw CreatePlanError.limitReached }

        try await planRepository.save(plan)
        try await reschedule.execute()
    }
}
