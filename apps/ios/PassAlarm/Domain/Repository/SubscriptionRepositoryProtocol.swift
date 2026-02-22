import Foundation

protocol SubscriptionRepositoryProtocol: Sendable {
    func observeStatus() -> AsyncStream<ProStatus>
    func currentStatus() async -> ProStatus
    func fetchProducts() async throws -> [ProProduct]
    func purchase(_ product: ProProduct) async throws -> ProStatus
    func restorePurchases() async throws -> ProStatus
}
