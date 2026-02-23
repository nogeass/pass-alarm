import Foundation

final class UpdateAppSettingsUseCase: Sendable {
    private let appSettingsRepository: AppSettingsRepositoryProtocol
    private let reschedule: RescheduleNextNUseCase

    init(appSettingsRepository: AppSettingsRepositoryProtocol,
         reschedule: RescheduleNextNUseCase) {
        self.appSettingsRepository = appSettingsRepository
        self.reschedule = reschedule
    }

    func execute(_ settings: AppSettings) async throws {
        await appSettingsRepository.save(settings)
        try await reschedule.execute()
    }
}
