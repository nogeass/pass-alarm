package com.nogeass.passalarm.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.nogeass.passalarm.R
import com.nogeass.passalarm.domain.model.AlarmSession
import com.nogeass.passalarm.notification.NotificationChannelManager
import com.nogeass.passalarm.presentation.alarm.AlarmRingingActivity
import com.nogeass.passalarm.receiver.AlarmReceiver

/**
 * Foreground service that manages an alarm-ringing session.
 *
 * Key design: One OS-level alarm trigger starts one session that may ring
 * up to [totalRings] times with [intervalMin]-minute intervals between
 * each ring. The user can **stop** (= woke up) or **snooze** (= ring again
 * after the interval). When all rings are exhausted the session ends
 * automatically.
 *
 * Intent actions handled:
 *  - default / null  → start a new session (or re-ring on snooze timer)
 *  - [ACTION_STOP]   → user pressed "起きた"; end session immediately
 *  - [ACTION_SNOOZE] → user pressed "スヌーズ"; silence now, ring again later
 */
class AlarmForegroundService : Service() {

    companion object {
        private const val TAG = "AlarmForegroundService"
        const val NOTIFICATION_ID = 1001

        const val ACTION_STOP = "com.nogeass.passalarm.ACTION_STOP"
        const val ACTION_SNOOZE = "com.nogeass.passalarm.ACTION_SNOOZE"

        const val EXTRA_REPEAT_COUNT = "extra_repeat_count"
        const val EXTRA_INTERVAL_MIN = "extra_interval_min"
        const val EXTRA_SOUND_ID = "extra_sound_id"

        private const val WAKELOCK_TAG = "passalarm:alarm_session"
        private const val WAKELOCK_TIMEOUT_MS = 5L * 60 * 1000 // 5 min per ring max
    }

    // ── Session state ────────────────────────────────────────────────────

    private var session: AlarmSession = AlarmSession.Idle
    private var tokenId: Long = -1
    private var osIdentifier: Int = -1
    private var soundId: String = "default"

    // ── Resources ────────────────────────────────────────────────────────

    private var soundPlayer: AlarmSoundPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val handler = Handler(Looper.getMainLooper())
    private var snoozeRunnable: Runnable? = null

    // ── Service lifecycle ────────────────────────────────────────────────

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> handleStop()
            ACTION_SNOOZE -> handleSnooze()
            else -> handleStartOrNextRing(intent)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        cleanup()
        super.onDestroy()
    }

    // ── Action handlers ──────────────────────────────────────────────────

    /**
     * Start a new session or advance to the next ring when the snooze timer
     * fires (resends a plain intent to this service).
     */
    private fun handleStartOrNextRing(intent: Intent?) {
        tokenId = intent?.getLongExtra(AlarmReceiver.EXTRA_TOKEN_ID, tokenId) ?: tokenId
        osIdentifier = intent?.getIntExtra(AlarmReceiver.EXTRA_OS_IDENTIFIER, osIdentifier) ?: osIdentifier
        soundId = intent?.getStringExtra(EXTRA_SOUND_ID) ?: soundId

        val currentSession = session
        val ringing: AlarmSession.Ringing = when (currentSession) {
            is AlarmSession.Idle -> {
                // Brand-new session: extract plan parameters from extras.
                val repeatCount = intent?.getIntExtra(EXTRA_REPEAT_COUNT, 10) ?: 10
                val intervalMin = intent?.getIntExtra(EXTRA_INTERVAL_MIN, 5) ?: 5
                AlarmSession.Ringing(
                    tokenId = tokenId,
                    planId = 0, // not strictly needed at service level
                    totalRings = repeatCount,
                    intervalMin = intervalMin,
                    currentRingIndex = 1,
                )
            }
            is AlarmSession.Ringing -> {
                // Snooze timer expired → advance to next ring.
                currentSession.copy(currentRingIndex = currentSession.currentRingIndex + 1)
            }
        }

        if (ringing.isComplete) {
            Log.d(TAG, "All ${ringing.totalRings} rings completed, ending session")
            endSession()
            return
        }

        session = ringing
        Log.d(TAG, "Ringing ${ringing.currentRingIndex}/${ringing.totalRings}")

        acquireWakeLock()
        startRinging()
        showNotification(ringing)
        launchRingingActivity(ringing)
    }

    /** User pressed "起きた" — stop everything and end session. */
    private fun handleStop() {
        Log.d(TAG, "User stopped alarm")
        endSession()
    }

    /**
     * User pressed "スヌーズ" — silence now and schedule the next ring
     * after [AlarmSession.Ringing.intervalMin] minutes.
     */
    private fun handleSnooze() {
        val ringing = session as? AlarmSession.Ringing ?: run {
            endSession()
            return
        }

        Log.d(TAG, "Snooze: next ring in ${ringing.intervalMin} min")

        // Silence immediately.
        stopRinging()
        releaseWakeLock()

        // Check if we've already exhausted all rings.
        val nextIndex = ringing.currentRingIndex + 1
        if (nextIndex > ringing.totalRings) {
            Log.d(TAG, "No more rings left after snooze, ending session")
            endSession()
            return
        }

        // Update notification to show snoozing state.
        showSnoozingNotification(ringing)

        // Schedule the next ring using Handler (in-process timer, not OS alarm).
        val delayMs = ringing.intervalMin.toLong() * 60 * 1000
        val runnable = Runnable {
            // Re-deliver a plain start intent to ourselves.
            val nextIntent = Intent(this, AlarmForegroundService::class.java).apply {
                putExtra(AlarmReceiver.EXTRA_TOKEN_ID, tokenId)
                putExtra(AlarmReceiver.EXTRA_OS_IDENTIFIER, osIdentifier)
            }
            startService(nextIntent)
        }
        snoozeRunnable = runnable
        handler.postDelayed(runnable, delayMs)
    }

    // ── Sound / vibration ────────────────────────────────────────────────

    private fun startRinging() {
        stopRinging() // safety: stop any previous playback
        soundPlayer = AlarmSoundPlayer(this).also { it.start(soundId) }
    }

    private fun stopRinging() {
        soundPlayer?.stop()
        soundPlayer = null
    }

    // ── Notification ─────────────────────────────────────────────────────

    private fun showNotification(ringing: AlarmSession.Ringing) {
        val fullScreenIntent = buildRingingActivityIntent(ringing)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val stopPendingIntent = buildActionPendingIntent(ACTION_STOP, 1)
        val snoozePendingIntent = buildActionPendingIntent(ACTION_SNOOZE, 2)

        val notification = NotificationCompat.Builder(this, NotificationChannelManager.CHANNEL_ALARM_RINGING)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("${getString(R.string.ringing_alarm)} ${ringing.progressText}")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .addAction(0, getString(R.string.ringing_stop), stopPendingIntent)
            .addAction(0, getString(R.string.ringing_snooze), snoozePendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun showSnoozingNotification(ringing: AlarmSession.Ringing) {
        val nextRing = ringing.currentRingIndex + 1
        val notification = NotificationCompat.Builder(this, NotificationChannelManager.CHANNEL_ALARM_RINGING)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("${getString(R.string.ringing_snooze)} — ${ringing.intervalMin}分後 ($nextRing/${ringing.totalRings})")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .build()

        val nm = getSystemService(android.app.NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, notification)
    }

    // ── Activity launch ──────────────────────────────────────────────────

    private fun launchRingingActivity(ringing: AlarmSession.Ringing) {
        val intent = buildRingingActivityIntent(ringing)
        startActivity(intent)
    }

    private fun buildRingingActivityIntent(ringing: AlarmSession.Ringing): Intent {
        return Intent(this, AlarmRingingActivity::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TOKEN_ID, tokenId)
            putExtra(AlarmReceiver.EXTRA_OS_IDENTIFIER, osIdentifier)
            putExtra(AlarmRingingActivity.EXTRA_CURRENT_RING, ringing.currentRingIndex)
            putExtra(AlarmRingingActivity.EXTRA_TOTAL_RINGS, ringing.totalRings)
            putExtra(AlarmRingingActivity.EXTRA_INTERVAL_MIN, ringing.intervalMin)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
    }

    // ── PendingIntent helpers ────────────────────────────────────────────

    private fun buildActionPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, AlarmForegroundService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    // ── WakeLock ─────────────────────────────────────────────────────────

    private fun acquireWakeLock() {
        releaseWakeLock()
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            WAKELOCK_TAG,
        ).apply {
            acquire(WAKELOCK_TIMEOUT_MS)
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) wakeLock?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing wakelock", e)
        }
        wakeLock = null
    }

    // ── Session end & cleanup ────────────────────────────────────────────

    private fun endSession() {
        cleanup()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun cleanup() {
        // Cancel pending snooze timer.
        snoozeRunnable?.let { handler.removeCallbacks(it) }
        snoozeRunnable = null

        stopRinging()
        releaseWakeLock()

        session = AlarmSession.Idle
    }
}
