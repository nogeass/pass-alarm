import Foundation

final class CheckProLimitUseCase: Sendable {
    private let planRepository: AlarmPlanRepositoryProtocol
    private let subscriptionRepository: SubscriptionRepositoryProtocol

    private static let freePlanLimit = 10
    private static let proPlanLimit = 100

    init(planRepository: AlarmPlanRepositoryProtocol,
         subscriptionRepository: SubscriptionRepositoryProtocol) {
        self.planRepository = planRepository
        self.subscriptionRepository = subscriptionRepository
    }

    /// Returns `true` if the user can create a new alarm plan within their tier limit.
    func execute() async throws -> Bool {
        let status = await subscriptionRepository.currentStatus()
        let plans = try await planRepository.fetchAll()
        let limit = status.isPro ? Self.proPlanLimit : Self.freePlanLimit
        return plans.count < limit
    }
}
