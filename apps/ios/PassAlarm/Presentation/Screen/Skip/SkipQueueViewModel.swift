import Foundation

@Observable
final class SkipQueueViewModel {
    var queue: [Occurrence] = []
    var isPro: Bool = false
    var showProPurchase: Bool = false
    var holidayAutoSkip: Bool = true
    var showToast = false
    var toastMessage = ""

    private let container: DIContainer

    init(container: DIContainer) {
        self.container = container
    }

    func load() async {
        do {
            queue = try await container.computeQueueUseCase.execute()
            let status = await container.subscriptionRepository.currentStatus()
            isPro = status.isPro
            let settings = await container.appSettingsRepository.get()
            holidayAutoSkip = settings.holidayAutoSkip
        } catch {
            print("SkipQueueViewModel load error: \(error)")
        }
    }

    func skip(planId: UUID, date: String) async {
        do {
            try await container.skipDateUseCase.execute(planId: planId, date: date)
            await load()
        } catch {
            print("Skip error: \(error)")
        }
    }

    func unskip(planId: UUID, date: String) async {
        do {
            try await container.skipDateUseCase.unskip(planId: planId, date: date)
            await load()
        } catch {
            print("Unskip error: \(error)")
        }
    }

    func toggleHolidayAutoSkip(_ enabled: Bool) async {
        do {
            var settings = await container.appSettingsRepository.get()
            settings.holidayAutoSkip = enabled
            try await container.updateAppSettingsUseCase.execute(settings)
            holidayAutoSkip = enabled
            await load()
        } catch {
            print("Toggle holiday auto-skip error: \(error)")
        }
    }
}
