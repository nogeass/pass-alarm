import Foundation
import UserNotifications

final class UNNotificationSchedulerAdapter: NotificationSchedulerProtocol {
    /// Maps soundId to the bundled .caf filename.
    private static let soundFileMap: [String: String] = [
        "default": "alarm_default.caf",
        "alarm": "alarm_alarm.caf",
        "beacon": "alarm_beacon.caf",
        "bulletin": "alarm_bulletin.caf",
        "radar": "alarm_radar.caf",
        "signal": "alarm_signal.caf",
    ]

    func schedule(identifier: String, at date: Date, title: String, body: String, soundId: String) async throws {
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body

        let fileName = Self.soundFileMap[soundId] ?? "alarm_default.caf"
        content.sound = UNNotificationSound(named: UNNotificationSoundName(fileName))
        content.interruptionLevel = .timeSensitive
        content.categoryIdentifier = "ALARM_RING"
        content.userInfo["soundId"] = soundId
        content.userInfo["isAlarm"] = true

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
