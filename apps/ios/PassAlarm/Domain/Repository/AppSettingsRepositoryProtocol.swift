import Foundation

protocol AppSettingsRepositoryProtocol: Sendable {
    func get() async -> AppSettings
    func save(_ settings: AppSettings) async
}
