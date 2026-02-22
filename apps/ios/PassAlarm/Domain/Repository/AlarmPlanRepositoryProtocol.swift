import Foundation

protocol AlarmPlanRepositoryProtocol: Sendable {
    func fetchAll() async throws -> [AlarmPlan]
    func fetchById(_ id: UUID) async throws -> AlarmPlan?
    func save(_ plan: AlarmPlan) async throws
    func delete(_ id: UUID) async throws
}
