import Foundation

@Observable
final class QueueViewModel {
    var queue: [Occurrence] = []
    var isPro: Bool = false
    var showProPurchase: Bool = false

    private let container: DIContainer

    init(container: DIContainer) {
        self.container = container
    }

    func load() async {
        do {
            queue = try await container.computeQueueUseCase.execute()
            let status = await container.subscriptionRepository.currentStatus()
            isPro = status.isPro
        } catch {
            print("QueueViewModel load error: \(error)")
        }
    }

    func skip(date: String) async {
        do {
            try await container.skipDateUseCase.execute(date: date)
            await load()
        } catch {
            print("Skip error: \(error)")
        }
    }

    func unskip(date: String) async {
        do {
            try await container.skipDateUseCase.unskip(date: date)
            await load()
        } catch {
            print("Unskip error: \(error)")
        }
    }
}
