import Foundation

@Observable
final class HomeViewModel {
    var plan: AlarmPlan?
    var nextOccurrence: Occurrence?
    var queue: [Occurrence] = []

    private let container: DIContainer

    init(container: DIContainer) {
        self.container = container
    }

    func load() async {
        do {
            let plans = try await container.alarmPlanRepository.fetchAll()
            plan = plans.first
            if plan == nil {
                let defaultPlan = AlarmPlan.default
                try await container.alarmPlanRepository.save(defaultPlan)
                plan = defaultPlan
            }
            queue = try await container.computeQueueUseCase.execute()
            nextOccurrence = queue.first(where: { !$0.isSkipped })
        } catch {
            print("HomeViewModel load error: \(error)")
        }
    }

    func togglePlan(_ isEnabled: Bool) async {
        guard let plan else { return }
        do {
            try await container.enablePlanUseCase.execute(planId: plan.id, isEnabled: isEnabled)
            await load()
        } catch {
            print("Toggle error: \(error)")
        }
    }

    func skipToday() async {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        formatter.locale = Locale(identifier: "en_US_POSIX")
        let today = formatter.string(from: Date())

        do {
            try await container.skipDateUseCase.execute(date: today)
            await load()
        } catch {
            print("Skip error: \(error)")
        }
    }
}
