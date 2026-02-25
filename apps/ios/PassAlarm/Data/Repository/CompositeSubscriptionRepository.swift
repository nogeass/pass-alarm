import Foundation

/// Combines StoreKit subscriptions with server-side entitlements (crowdfunding, manual grants).
/// If either source grants Pro, the user is treated as Pro.
final class CompositeSubscriptionRepository: SubscriptionRepositoryProtocol, @unchecked Sendable {

    private let storeKitRepository: SubscriptionRepositoryProtocol
    private let serverEntitlementRepository: ServerEntitlementRepositoryProtocol

    init(
        storeKitRepository: SubscriptionRepositoryProtocol,
        serverEntitlementRepository: ServerEntitlementRepositoryProtocol
    ) {
        self.storeKitRepository = storeKitRepository
        self.serverEntitlementRepository = serverEntitlementRepository
    }

    // MARK: - Observe Status

    func observeStatus() -> AsyncStream<ProStatus> {
        let storeKit = storeKitRepository
        let server = serverEntitlementRepository

        return AsyncStream { continuation in
            let task = Task {
                for await storeKitStatus in storeKit.observeStatus() {
                    let merged = Self.mergeStatus(
                        storeKit: storeKitStatus,
                        serverEntitlements: server.cachedEntitlements()
                    )
                    continuation.yield(merged)
                }
                continuation.finish()
            }
            continuation.onTermination = { _ in
                task.cancel()
            }
        }
    }

    // MARK: - Current Status

    func currentStatus() async -> ProStatus {
        let storeKitStatus = await storeKitRepository.currentStatus()
        let serverEntitlements = serverEntitlementRepository.cachedEntitlements()
        return Self.mergeStatus(storeKit: storeKitStatus, serverEntitlements: serverEntitlements)
    }

    // MARK: - Delegate to StoreKit

    func fetchProducts() async throws -> [ProProduct] {
        try await storeKitRepository.fetchProducts()
    }

    func purchase(_ product: ProProduct) async throws -> ProStatus {
        try await storeKitRepository.purchase(product)
    }

    func restorePurchases() async throws -> ProStatus {
        try await storeKitRepository.restorePurchases()
    }

    // MARK: - Merge Logic

    /// Returns the "best" status from StoreKit and server entitlements.
    /// Crowdfund entitlements without an expiration date are treated as lifetime Pro.
    private static func mergeStatus(
        storeKit: ProStatus,
        serverEntitlements: [ServerEntitlement]
    ) -> ProStatus {
        // Check server entitlements for active Pro
        let now = Date()
        let activeServerEntitlement = serverEntitlements.first { entitlement in
            guard entitlement.tier == .pro else { return false }
            // No expiry = lifetime (e.g., crowdfund)
            if entitlement.expiresAt == nil { return true }
            // Has expiry: check if still valid
            return entitlement.expiresAt! > now
        }

        // If StoreKit says Pro AND server has an active entitlement, prefer the "better" one
        if let serverEntitlement = activeServerEntitlement {
            // Crowdfund / lifetime entitlements take precedence
            if serverEntitlement.isLifetime {
                return ProStatus(
                    tier: .pro,
                    expiresAt: nil,
                    period: nil,
                    source: serverEntitlement.source
                )
            }
            // If StoreKit is also Pro, prefer StoreKit (it has period info)
            if storeKit.isPro {
                return storeKit
            }
            // Otherwise use server entitlement
            return ProStatus(
                tier: .pro,
                expiresAt: serverEntitlement.expiresAt,
                period: nil,
                source: serverEntitlement.source
            )
        }

        // Only StoreKit
        return storeKit
    }
}
