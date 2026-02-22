package com.nogeass.passalarm.presentation.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Full-screen overlay that shows a praise [message] and auto-dismisses
 * after 2 seconds.
 *
 * Mirrors the iOS PraiseToast component – capsule shaped, slides in from
 * the bottom, fades out.
 *
 * @param message   Text to display.
 * @param isVisible Whether the toast is currently shown.
 * @param onDismiss Called when the toast should hide.
 */
@Composable
fun PraiseToast(
    message: String,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Auto-dismiss after 2 seconds
    if (isVisible) {
        LaunchedEffect(message) {
            delay(2_000L)
            onDismiss()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
        ) {
            Text(
                text = message,
                style = PassTypography.toastText,
                color = Color.White,
                modifier = Modifier
                    .padding(bottom = 64.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.70f))
                    .padding(
                        horizontal = PassSpacing.lg,
                        vertical = PassSpacing.md,
                    ),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Praise message pool – identical to the iOS PraiseMessages enum.
// ─────────────────────────────────────────────────────────────────────────────

object PraiseMessages {

    // ── Message pools ────────────────────────────────────────────────────

    private val wakeUp = listOf(
        "起きた。えらすぎ。",
        "今日も勝ち",
        "天才かもしれない",
        "偉業を達成した",
        "朝を制した",
        "ナイス起床",
        "今日のヒーローはあなた",
        "起きた、天才",
    )

    private val skip = listOf(
        "今日はパスでOK",
        "無理しないのが正解",
        "パスした",
        "いいね、休もう",
        "今日はゆっくり",
    )

    private val settingsComplete = listOf(
        "準備OK。任せて",
        "セット完了",
        "いい感じ",
        "明日から起こすね",
    )

    private val purchase = listOf(
        "最高。これで無限。",
        "ありがとう",
        "アップグレード完了",
    )

    // ── Public API ───────────────────────────────────────────────────────

    /** 80 % chance to return a wake-up praise message, null otherwise. */
    fun randomWakeUp(): String? =
        if (Random.nextFloat() < 0.8f) wakeUp.random() else null

    /** 50 % chance to return a skip message, null otherwise. */
    fun randomSkip(): String? =
        if (Random.nextFloat() < 0.5f) skip.random() else null

    /** Always returns a settings-complete message. */
    fun randomSettingsComplete(): String =
        settingsComplete.random()

    /** Always returns a purchase thank-you message. */
    fun randomPurchase(): String =
        purchase.random()
}
