import Foundation

final class RescheduleNextNUseCase: Sendable {
    private let computeQueue: ComputeQueueUseCase
    private let tokenRepository: ScheduledTokenRepositoryProtocol
    private let scheduler: NotificationSchedulerProtocol

    static let scheduleCount = 10

    init(computeQueue: ComputeQueueUseCase,
         tokenRepository: ScheduledTokenRepositoryProtocol,
         scheduler: NotificationSchedulerProtocol) {
        self.computeQueue = computeQueue
        self.tokenRepository = tokenRepository
        self.scheduler = scheduler
    }

    func execute() async throws {
        // 1. Cancel all existing scheduled tokens
        let existing = try await tokenRepository.fetchScheduled()
        let existingIds = existing.map { $0.osIdentifier }
        if !existingIds.isEmpty {
            try await scheduler.cancel(identifiers: existingIds)
        }
        try await tokenRepository.deleteScheduled()

        // 2. Compute fresh queue
        let queue = try await computeQueue.execute()
        let activeOccurrences = queue.filter { !$0.isSkipped }
        let topN = Array(activeOccurrences.prefix(Self.scheduleCount))

        // 3. Schedule new tokens
        var tokens: [ScheduledToken] = []
        for occurrence in topN {
            let osId = "passalarm-\(occurrence.date)-\(occurrence.timeHHmm.replacingOccurrences(of: ":", with: ""))"
            let token = ScheduledToken(
                id: UUID(),
                date: occurrence.date,
                fireAtEpoch: occurrence.fireDate.timeIntervalSince1970,
                osIdentifier: osId,
                status: .scheduled,
                createdAt: Date(),
                updatedAt: Date()
            )
            tokens.append(token)

            try await scheduler.schedule(
                identifier: osId,
                at: occurrence.fireDate,
                title: "パスアラーム",
                body: "\(occurrence.timeHHmm) アラーム"
            )
        }

        try await tokenRepository.saveAll(tokens)
    }
}
