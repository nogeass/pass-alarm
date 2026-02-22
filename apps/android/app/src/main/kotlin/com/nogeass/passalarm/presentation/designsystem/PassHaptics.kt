package com.nogeass.passalarm.presentation.designsystem

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView

/**
 * PassAlarm haptic-feedback helpers.
 *
 * Mirrors the iOS PassHaptics enum using Android's [HapticFeedbackConstants]
 * via the View API, which requires no VIBRATE permission.
 */
object PassHaptics {

    /** Light tap – normal button press. */
    fun tap(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    /** Medium impact – skip confirm, important actions. */
    fun medium(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
    }

    /** Success notification – wake-up stop, purchase success. */
    fun success(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    /** Warning – permission denied, error states. */
    fun warning(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
}

/**
 * Enum matching [PassButton]'s haptic parameter.
 */
enum class PassHapticType {
    Tap,
    Medium,
    Success,
}

/**
 * Convenience Compose accessor – returns the current [View] so callers can
 * do `val view = rememberHapticView(); PassHaptics.tap(view)`.
 */
@Composable
fun rememberHapticView(): View = LocalView.current
