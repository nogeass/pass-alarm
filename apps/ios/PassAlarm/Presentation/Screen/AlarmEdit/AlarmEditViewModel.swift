import Foundation

@Observable
final class AlarmEditViewModel {
    var selectedHour: Int = 7
    var selectedMinute: Int = 0  // 10-minute increments (0,10,20,30,40,50)
    var label: String = ""
    var weekdaysMask: UInt8 = 0b00011111
    var repeatCount: Int = 10
    var intervalMin: Int = 5
    var soundId: String = "default"
    var isEditing: Bool = false
    var showLimitError: Bool = false

    static let minuteOptions = stride(from: 0, to: 60, by: 10).map { $0 }

    private var planId: UUID?
    private let container: DIContainer

    init(container: DIContainer, plan: AlarmPlan?) {
        self.container = container
        if let plan = plan {
            self.planId = plan.id
            self.isEditing = true
            self.label = plan.label
            self.weekdaysMask = plan.weekdaysMask
            self.repeatCount = plan.repeatCount
            self.intervalMin = plan.intervalMin
            self.soundId = plan.soundId

            let parts = plan.timeHHmm.split(separator: ":").compactMap { Int($0) }
            if parts.count == 2 {
                self.selectedHour = parts[0]
                // Round to nearest 10
                self.selectedMinute = (parts[1] / 10) * 10
            }
        }
    }

    func save() async -> Bool {
        let timeStr = String(format: "%02d:%02d", selectedHour, selectedMinute)

        if isEditing, let planId = planId {
            var plan = AlarmPlan(
                id: planId,
                isEnabled: true,
                label: label,
                timeHHmm: timeStr,
                weekdaysMask: weekdaysMask,
                repeatCount: repeatCount,
                intervalMin: intervalMin,
                soundId: soundId,
                createdAt: Date(),
                updatedAt: Date()
            )
            do {
                try await container.updatePlanUseCase.execute(plan)
                return true
            } catch {
                print("AlarmEditViewModel update error: \(error)")
                return false
            }
        } else {
            let plan = AlarmPlan(
                id: UUID(),
                isEnabled: true,
                label: label,
                timeHHmm: timeStr,
                weekdaysMask: weekdaysMask,
                repeatCount: repeatCount,
                intervalMin: intervalMin,
                soundId: soundId,
                createdAt: Date(),
                updatedAt: Date()
            )
            do {
                try await container.createPlanUseCase.execute(plan)
                return true
            } catch let error as CreatePlanUseCase.CreatePlanError {
                if error == .limitReached {
                    showLimitError = true
                }
                return false
            } catch {
                print("AlarmEditViewModel create error: \(error)")
                return false
            }
        }
    }

    func delete() async {
        guard let planId = planId else { return }
        try? await container.deletePlanUseCase.execute(planId: planId)
    }
}
