import Foundation

enum TokenStatus: String, Codable, Sendable {
    case scheduled = "SCHEDULED"
    case canceled = "CANCELED"
    case fired = "FIRED"
}
