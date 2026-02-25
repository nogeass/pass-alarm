import Foundation

struct ServerEntitlement: Sendable, Equatable {
    let id: Int
    let tier: ProTier
    let source: ProSource
    let grantedAt: Date
    let expiresAt: Date?

    var isLifetime: Bool { source == .crowdfund && expiresAt == nil }
}

/// Codable support using ISO 8601 dates
extension ServerEntitlement: Codable {
    enum CodingKeys: String, CodingKey {
        case id, tier, source, grantedAt, expiresAt
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        id = try container.decode(Int.self, forKey: .id)
        tier = try container.decode(ProTier.self, forKey: .tier)
        source = try container.decode(ProSource.self, forKey: .source)
        let grantedAtStr = try container.decode(String.self, forKey: .grantedAt)
        grantedAt = Self.parseDate(grantedAtStr) ?? Date()
        let expiresAtStr = try container.decodeIfPresent(String.self, forKey: .expiresAt)
        expiresAt = expiresAtStr.flatMap { Self.parseDate($0) }
    }

    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(id, forKey: .id)
        try container.encode(tier, forKey: .tier)
        try container.encode(source, forKey: .source)
        try container.encode(ISO8601DateFormatter().string(from: grantedAt), forKey: .grantedAt)
        try container.encodeIfPresent(expiresAt.map { ISO8601DateFormatter().string(from: $0) }, forKey: .expiresAt)
    }

    private static func parseDate(_ string: String) -> Date? {
        let f1 = ISO8601DateFormatter()
        f1.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        if let d = f1.date(from: string) { return d }
        let f2 = ISO8601DateFormatter()
        f2.formatOptions = [.withInternetDateTime]
        return f2.date(from: string)
    }
}

protocol ServerEntitlementRepositoryProtocol: Sendable {
    func fetchEntitlements() async throws -> [ServerEntitlement]
    func cachedEntitlements() -> [ServerEntitlement]
    func claimToken(_ token: String) async throws -> ServerEntitlement
    func isRedeemDisabled() async throws -> Bool
}
