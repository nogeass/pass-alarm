import XCTest

final class ScreenshotTests: XCTestCase {

    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        setupSnapshot(app)
        app.launch()
    }

    func testScreenshots() throws {
        // 1. ホーム画面（アラームキュー）
        snapshot("01_alarm_queue")

        // 2. アラーム作成画面
        let addButton = app.buttons["addAlarm"]
        if addButton.waitForExistence(timeout: 3) {
            addButton.tap()
            snapshot("02_create_alarm")

            // 戻る
            let backButton = app.navigationBars.buttons.firstMatch
            if backButton.exists {
                backButton.tap()
            }
        }

        // 3. スキップ操作（キュー上でスワイプ）
        let firstCell = app.cells.firstMatch
        if firstCell.waitForExistence(timeout: 3) {
            firstCell.swipeLeft()
            snapshot("03_skip_today")
        }

        // 4. 設定画面
        let settingsButton = app.buttons["settings"]
        if settingsButton.waitForExistence(timeout: 3) {
            settingsButton.tap()
            snapshot("04_settings")
        }
    }
}
