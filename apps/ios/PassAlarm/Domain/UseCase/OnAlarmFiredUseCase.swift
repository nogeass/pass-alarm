import Foundation

final class OnAlarmFiredUseCase: Sendable {
    private let tokenRepository: ScheduledTokenRepositoryProtocol
    private let reschedule: RescheduleNextNUseCase

    init(tokenRepository: ScheduledTokenRepositoryProtocol,
         reschedule: RescheduleNextNUseCase) {
        self.tokenRepository = tokenRepository
        self.reschedule = reschedule
    }

    func execute(osIdentifier: String) async {
        do {
            if let token = try await tokenRepository.fetchByOsIdentifier(osIdentifier) {
                try await tokenRepository.updateStatus(token.id, status: .fired)
            }
            try await reschedule.execute()
        } catch {
            print("OnAlarmFiredUseCase error: \(error)")
        }
    }
}
