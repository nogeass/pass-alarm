import Foundation

enum ProTier: String, Sendable, Codable {
    case free, pro
}

enum ProPeriod: String, Sendable, Codable {
    case monthly, yearly
}

enum ProSource: String, Sendable, Codable {
    case store
    case crowdfund
    case manual
}

struct ProStatus: Sendable, Equatable {
    let tier: ProTier
    let expiresAt: Date?
    let period: ProPeriod?
    let source: ProSource

    var isPro: Bool { tier == .pro }
    var isLifetime: Bool { isPro && source == .crowdfund && expiresAt == nil }
    static let free = ProStatus(tier: .free, expiresAt: nil, period: nil, source: .store)

    init(tier: ProTier, expiresAt: Date?, period: ProPeriod?, source: ProSource = .store) {
        self.tier = tier
        self.expiresAt = expiresAt
        self.period = period
        self.source = source
    }
}

struct ProProduct: Identifiable, Sendable {
    let id: String
    let period: ProPeriod
    let displayPrice: String
    let pricePerMonth: String?
    let trialText: String?
}
