import UIKit
import UserNotifications
import FirebaseCore

final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        FirebaseApp.configure()
        UNUserNotificationCenter.current().delegate = self
        registerNotificationCategories()
        return true
    }

    private func registerNotificationCategories() {
        let stopAction = UNNotificationAction(
            identifier: "STOP_ACTION",
            title: "起きた",
            options: [.destructive, .authenticationRequired]
        )
        let snoozeAction = UNNotificationAction(
            identifier: "SNOOZE_ACTION",
            title: "スヌーズ",
            options: []
        )
        let alarmCategory = UNNotificationCategory(
            identifier: "ALARM_RING",
            actions: [stopAction, snoozeAction],
            intentIdentifiers: [],
            options: []
        )
        UNUserNotificationCenter.current().setNotificationCategories([alarmCategory])
    }

    static let alarmFiredNotification = Notification.Name("PassAlarm.alarmFired")

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification
    ) async -> UNNotificationPresentationOptions {
        let userInfo = notification.request.content.userInfo
        if userInfo["isAlarm"] as? Bool == true {
            let soundId = userInfo["soundId"] as? String ?? "default"
            let osId = notification.request.identifier
            await MainActor.run {
                NotificationCenter.default.post(
                    name: Self.alarmFiredNotification,
                    object: nil,
                    userInfo: ["soundId": soundId, "osIdentifier": osId]
                )
            }
            return [.sound, .banner]
        }
        return [.sound, .banner]
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse
    ) async {
        let identifier = response.notification.request.identifier
        let actionId = response.actionIdentifier
        let userInfo = response.notification.request.content.userInfo

        switch actionId {
        case "STOP_ACTION":
            await DIContainer.shared.onAlarmFiredUseCase.execute(osIdentifier: identifier)
            await MainActor.run {
                NotificationCenter.default.post(
                    name: Notification.Name("PassAlarm.alarmStopped"),
                    object: nil
                )
            }
        case "SNOOZE_ACTION":
            await MainActor.run {
                NotificationCenter.default.post(
                    name: Notification.Name("PassAlarm.alarmSnoozed"),
                    object: nil
                )
            }
        case UNNotificationDefaultActionIdentifier:
            // User tapped the notification — show ringing view
            let soundId = userInfo["soundId"] as? String ?? "default"
            await MainActor.run {
                NotificationCenter.default.post(
                    name: Self.alarmFiredNotification,
                    object: nil,
                    userInfo: ["soundId": soundId, "osIdentifier": identifier]
                )
            }
        default:
            break
        }
    }
}
