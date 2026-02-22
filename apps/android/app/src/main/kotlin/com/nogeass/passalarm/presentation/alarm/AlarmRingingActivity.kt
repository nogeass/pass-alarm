package com.nogeass.passalarm.presentation.alarm

import android.app.KeyguardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nogeass.passalarm.presentation.theme.PassAlarmTheme
import com.nogeass.passalarm.receiver.AlarmReceiver
import com.nogeass.passalarm.service.AlarmForegroundService
import dagger.hilt.android.AndroidEntryPoint

/**
 * Full-screen activity shown when the alarm rings.
 *
 * Displays the Zenly-style ringing UI with stop ("起きた") and snooze buttons.
 * Communicates user actions to [AlarmForegroundService] via service intents.
 *
 * Manifest attributes `showWhenLocked` and `turnScreenOn` allow this
 * activity to appear over the lock screen. Programmatic flags provide
 * fallback for older API levels and keep the screen on while displayed.
 */
@AndroidEntryPoint
class AlarmRingingActivity : ComponentActivity() {

    companion object {
        const val EXTRA_CURRENT_RING = "extra_current_ring"
        const val EXTRA_TOTAL_RINGS = "extra_total_rings"
        const val EXTRA_INTERVAL_MIN = "extra_interval_min"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupLockScreenFlags()

        val currentRing = intent.getIntExtra(EXTRA_CURRENT_RING, 1)
        val totalRings = intent.getIntExtra(EXTRA_TOTAL_RINGS, 10)
        val intervalMin = intent.getIntExtra(EXTRA_INTERVAL_MIN, 5)
        val progressText = "$currentRing/$totalRings"

        setContent {
            PassAlarmTheme {
                AlarmRingingScreen(
                    progressText = progressText,
                    onStop = { sendStopToService() },
                    onSnooze = { sendSnoozeToService() },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Recreate to pick up new progress values when snooze timer fires.
        recreate()
    }

    // ── Lock-screen & keep-awake flags ───────────────────────────────────

    private fun setupLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(KeyguardManager::class.java)
            km.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        // Keep screen on while this activity is displayed.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // ── Service communication ────────────────────────────────────────────

    private fun sendStopToService() {
        val intent = Intent(this, AlarmForegroundService::class.java).apply {
            action = AlarmForegroundService.ACTION_STOP
        }
        startService(intent)
        finish()
    }

    private fun sendSnoozeToService() {
        val intent = Intent(this, AlarmForegroundService::class.java).apply {
            action = AlarmForegroundService.ACTION_SNOOZE
        }
        startService(intent)
        finish()
    }
}
