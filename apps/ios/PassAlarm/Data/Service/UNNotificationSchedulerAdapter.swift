import Foundation
import UserNotifications

final class UNNotificationSchedulerAdapter: NotificationSchedulerProtocol {
    func schedule(identifier: String, at date: Date, title: String, body: String) async throws {
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        content.sound = .default
        content.categoryIdentifier = "ALARM_RING"

        let calendar = Calendar.current
        let components = calendar.dateComponents([.year, .month, .day, .hour, .minute, .second], from: date)
        let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: false)

        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)
        try await UNUserNotificationCenter.current().add(request)
    }

    func cancel(identifiers: [String]) async throws {
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: identifiers)
    }

    func cancelAll() async throws {
        UNUserNotificationCenter.current().removeAllPendingNotificationRequests()
    }
}
