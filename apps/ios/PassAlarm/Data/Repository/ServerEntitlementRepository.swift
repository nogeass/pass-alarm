import Foundation

final class ServerEntitlementRepository: ServerEntitlementRepositoryProtocol, @unchecked Sendable {

    private let authService: AuthServiceProtocol
    private let userDefaults: UserDefaults
    private let cacheKey = "ServerEntitlementRepository.cachedEntitlements"

    private static let iso8601Formatter: ISO8601DateFormatter = {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        return formatter
    }()

    private static let iso8601FormatterNoFraction: ISO8601DateFormatter = {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime]
        return formatter
    }()

    init(authService: AuthServiceProtocol, userDefaults: UserDefaults = .standard) {
        self.authService = authService
        self.userDefaults = userDefaults
    }

    // MARK: - Fetch Entitlements

    func fetchEntitlements() async throws -> [ServerEntitlement] {
        let idToken = try await authService.getIDToken()
        let response = try await PassAlarmAPI.getEntitlements(idToken: idToken)
        let entitlements = response.entitlements.compactMap { mapToServerEntitlement($0) }
        saveToCache(entitlements)
        return entitlements
    }

    // MARK: - Cached Entitlements

    func cachedEntitlements() -> [ServerEntitlement] {
        guard let data = userDefaults.data(forKey: cacheKey) else { return [] }
        do {
            return try JSONDecoder().decode([ServerEntitlement].self, from: data)
        } catch {
            print("[ServerEntitlementRepository] Failed to decode cached entitlements: \(error)")
            return []
        }
    }

    // MARK: - Claim Token

    func claimToken(_ token: String) async throws -> ServerEntitlement {
        let idToken = try await authService.getIDToken()
        let response = try await PassAlarmAPI.claimToken(token, idToken: idToken)

        guard let entitlement = response.entitlement else {
            throw PassAlarmAPIError.serverError(statusCode: 0, message: response.error)
        }

        let serverEntitlement = ServerEntitlement(
            id: 0, // Server doesn't return id on claim
            tier: entitlement.tier == "pro" ? .pro : .free,
            source: ProSource(rawValue: entitlement.source) ?? .crowdfund,
            grantedAt: Self.parseDate(entitlement.grantedAt) ?? Date(),
            expiresAt: nil
        )

        // Refresh cache after claiming
        var cached = cachedEntitlements()
        cached.append(serverEntitlement)
        saveToCache(cached)

        return serverEntitlement
    }

    // MARK: - Config

    func isRedeemDisabled() async throws -> Bool {
        let config = try await PassAlarmAPI.getConfig()
        return config.redeemDisabled == "true"
    }

    // MARK: - Private Helpers

    private func mapToServerEntitlement(_ item: EntitlementsResponse.EntitlementItem) -> ServerEntitlement? {
        guard let tier = ProTier(rawValue: item.tier),
              let grantedAt = Self.parseDate(item.grantedAt) else {
            return nil
        }

        let source = ProSource(rawValue: item.source) ?? .manual
        let expiresAt = item.expiresAt.flatMap { Self.parseDate($0) }

        return ServerEntitlement(
            id: item.id,
            tier: tier,
            source: source,
            grantedAt: grantedAt,
            expiresAt: expiresAt
        )
    }

    private func saveToCache(_ entitlements: [ServerEntitlement]) {
        do {
            let data = try JSONEncoder().encode(entitlements)
            userDefaults.set(data, forKey: cacheKey)
        } catch {
            print("[ServerEntitlementRepository] Failed to cache entitlements: \(error)")
        }
    }

    private static func parseDate(_ string: String) -> Date? {
        iso8601Formatter.date(from: string) ?? iso8601FormatterNoFraction.date(from: string)
    }
}
