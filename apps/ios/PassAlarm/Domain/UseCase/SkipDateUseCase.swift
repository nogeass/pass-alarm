import Foundation

final class SkipDateUseCase: Sendable {
    private let skipRepository: SkipExceptionRepositoryProtocol
    private let reschedule: RescheduleNextNUseCase

    init(skipRepository: SkipExceptionRepositoryProtocol,
         reschedule: RescheduleNextNUseCase) {
        self.skipRepository = skipRepository
        self.reschedule = reschedule
    }

    func execute(date: String, reason: SkipException.SkipReason = .manual) async throws {
        let skip = SkipException(
            id: UUID(),
            date: date,
            reason: reason,
            createdAt: Date()
        )
        try await skipRepository.save(skip)
        try await reschedule.execute()
    }

    func unskip(date: String) async throws {
        try await skipRepository.deleteByDate(date)
        try await reschedule.execute()
    }
}
