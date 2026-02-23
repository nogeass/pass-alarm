package com.nogeass.passalarm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class ScreenshotTest {

    companion object {
        @get:ClassRule
        @JvmStatic
        val localeTestRule = LocaleTestRule()
    }

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun takeScreenshots() {
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())

        // 1. ホーム画面（アラームキュー）
        Thread.sleep(2000)
        Screengrab.screenshot("01_alarm_queue")

        // 2. アラーム作成画面
        // TODO: Navigate to create alarm screen
        // onView(withId(R.id.fab_add_alarm)).perform(click())
        // Thread.sleep(1000)
        // Screengrab.screenshot("02_create_alarm")

        // 3. スキップ操作
        // TODO: Perform swipe to skip
        // Screengrab.screenshot("03_skip_today")

        // 4. 設定画面
        // TODO: Navigate to settings
        // Screengrab.screenshot("04_settings")
    }
}
