import Foundation

protocol NotificationSchedulerProtocol: Sendable {
    func schedule(identifier: String, at date: Date, title: String, body: String) async throws
    func cancel(identifiers: [String]) async throws
    func cancelAll() async throws
}
