import Foundation

enum ProTier: String, Sendable, Codable {
    case free, pro
}

enum ProPeriod: String, Sendable, Codable {
    case monthly, yearly
}

struct ProStatus: Sendable, Equatable {
    let tier: ProTier
    let expiresAt: Date?
    let period: ProPeriod?

    var isPro: Bool { tier == .pro }
    static let free = ProStatus(tier: .free, expiresAt: nil, period: nil)
}

struct ProProduct: Identifiable, Sendable {
    let id: String
    let period: ProPeriod
    let displayPrice: String
    let pricePerMonth: String?
}
