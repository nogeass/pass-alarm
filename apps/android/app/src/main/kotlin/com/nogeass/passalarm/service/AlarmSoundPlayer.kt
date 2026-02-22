package com.nogeass.passalarm.service

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Encapsulates alarm sound playback via [MediaPlayer] and vibration via [Vibrator].
 *
 * Lifecycle:
 *  1. [start] — begins looping alarm tone + vibration pattern
 *  2. [stop]  — silences and releases all resources
 *
 * Safe to call [stop] multiple times.
 */
class AlarmSoundPlayer(private val context: Context) {

    companion object {
        private const val TAG = "AlarmSoundPlayer"
        private val VIBRATION_PATTERN = longArrayOf(0, 500, 500) // off-on-off
        private const val VIBRATION_REPEAT_INDEX = 0 // repeat from index 0
    }

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    /** Start playing the alarm tone on loop and begin vibration pattern. */
    fun start() {
        startSound()
        startVibration()
    }

    /** Stop all sound and vibration, release resources. */
    fun stop() {
        stopSound()
        stopVibration()
    }

    // ── Sound ────────────────────────────────────────────────────────────

    private fun startSound() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, alarmUri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start alarm sound", e)
        }
    }

    private fun stopSound() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping alarm sound", e)
        } finally {
            mediaPlayer = null
        }
    }

    // ── Vibration ────────────────────────────────────────────────────────

    private fun startVibration() {
        try {
            vibrator = obtainVibrator()
            vibrator?.let { vib ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vib.vibrate(
                        VibrationEffect.createWaveform(VIBRATION_PATTERN, VIBRATION_REPEAT_INDEX)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(VIBRATION_PATTERN, VIBRATION_REPEAT_INDEX)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start vibration", e)
        }
    }

    private fun stopVibration() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping vibration", e)
        } finally {
            vibrator = null
        }
    }

    private fun obtainVibrator(): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
}
