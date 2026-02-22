import Foundation

protocol SkipExceptionRepositoryProtocol: Sendable {
    func fetchAll() async throws -> [SkipException]
    func fetchByDateRange(from: String, to: String) async throws -> [SkipException]
    func save(_ skip: SkipException) async throws
    func delete(_ id: UUID) async throws
    func deleteByDate(_ date: String) async throws
}
