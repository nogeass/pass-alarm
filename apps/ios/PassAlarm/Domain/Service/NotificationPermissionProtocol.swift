import Foundation

enum PermissionStatus: Sendable {
    case notDetermined
    case authorized
    case denied
    case provisional
}

protocol NotificationPermissionProtocol: Sendable {
    func currentStatus() async -> PermissionStatus
    func request() async throws -> Bool
}
