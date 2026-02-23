import Foundation

@Observable
final class AlarmListViewModel {
    var plans: [AlarmPlan] = []

    private let container: DIContainer

    init(container: DIContainer) {
        self.container = container
    }

    func load() async {
        plans = (try? await container.alarmPlanRepository.fetchAll()) ?? []
    }

    func togglePlan(_ id: UUID, isEnabled: Bool) async {
        try? await container.enablePlanUseCase.execute(planId: id, isEnabled: isEnabled)
        await load()
    }

    func deletePlan(_ id: UUID) async {
        try? await container.deletePlanUseCase.execute(planId: id)
        await load()
    }
}
