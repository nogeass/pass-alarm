import Foundation

struct AppSettings: Equatable, Sendable, Codable {
    var holidayAutoSkip: Bool = true
    var tutorialCompleted: Bool = false
}
