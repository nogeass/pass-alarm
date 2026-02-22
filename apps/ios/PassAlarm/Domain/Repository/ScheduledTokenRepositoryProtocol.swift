import Foundation

protocol ScheduledTokenRepositoryProtocol: Sendable {
    func fetchAll() async throws -> [ScheduledToken]
    func fetchScheduled() async throws -> [ScheduledToken]
    func fetchByOsIdentifier(_ osId: String) async throws -> ScheduledToken?
    func save(_ token: ScheduledToken) async throws
    func saveAll(_ tokens: [ScheduledToken]) async throws
    func updateStatus(_ id: UUID, status: TokenStatus) async throws
    func deleteAll() async throws
    func deleteScheduled() async throws
}
