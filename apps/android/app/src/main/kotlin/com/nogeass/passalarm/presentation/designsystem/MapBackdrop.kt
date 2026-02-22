package com.nogeass.passalarm.presentation.designsystem

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Time-of-day enum with matching gradient colours from [PassColors].
 *
 * Maps 1-to-1 to the iOS TimeOfDay enum.
 */
enum class TimeOfDay(
    val gradientColors: List<Color>,
) {
    Morning(listOf(PassColors.morningStart, PassColors.morningEnd)),
    Noon(listOf(PassColors.noonStart, PassColors.noonEnd)),
    Evening(listOf(PassColors.eveningStart, PassColors.eveningEnd)),
    Night(listOf(PassColors.nightStart, PassColors.nightEnd)),
}

/**
 * Full-screen gradient background with softly floating translucent circles
 * to give a parallax / map-island feel, matching the iOS MapBackdrop view.
 *
 * @param timeOfDay Controls the gradient colours.
 * @param modifier  Outer modifier (typically none – the backdrop fills the screen).
 */
@Composable
fun MapBackdrop(
    timeOfDay: TimeOfDay,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "backdrop")

    // A slow horizontal drift shared by the floating circles.
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "drift",
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()

        Canvas(modifier = Modifier.fillMaxSize()) {
            // ── Gradient background ──────────────────────────────────────
            drawRect(
                brush = Brush.linearGradient(
                    colors = timeOfDay.gradientColors,
                    start = Offset.Zero,
                    end = Offset(size.width, size.height),
                ),
            )

            // ── Floating circles (parallax feel) ─────────────────────────
            drawCircle(
                color = Color.White.copy(alpha = 0.03f),
                radius = 150f,
                center = Offset(
                    x = w * 0.3f + drift * 0.5f,
                    y = h * 0.2f,
                ),
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = 100f,
                center = Offset(
                    x = w * 0.8f + drift * 0.375f,
                    y = h * 0.6f,
                ),
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.02f),
                radius = 75f,
                center = Offset(
                    x = w * 0.1f + drift * 0.25f,
                    y = h * 0.8f,
                ),
            )
        }
    }
}
