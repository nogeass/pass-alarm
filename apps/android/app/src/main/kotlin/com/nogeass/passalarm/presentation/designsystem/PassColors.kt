package com.nogeass.passalarm.presentation.designsystem

import androidx.compose.ui.graphics.Color

/**
 * PassAlarm design-system colour palette.
 *
 * Mirrors the iOS PassColors enum so both platforms share the exact same
 * hex values for time-of-day gradients, brand tints, semantic actions,
 * and surface colours.
 */
object PassColors {

    // ── Time-of-day gradients ────────────────────────────────────────────

    /** Warm yellow */
    val morningStart = Color(0xFFFFD18D)   // rgb(1.0, 0.82, 0.55)
    /** Soft orange */
    val morningEnd = Color(0xFFFF9E73)     // rgb(1.0, 0.62, 0.45)

    /** Sky blue */
    val noonStart = Color(0xFF87CEFF)      // rgb(0.53, 0.81, 1.0)
    /** Deeper blue */
    val noonEnd = Color(0xFF66ADF2)        // rgb(0.40, 0.68, 0.95)

    /** Sunset orange */
    val eveningStart = Color(0xFFF58F73)   // rgb(0.96, 0.56, 0.45)
    /** Purple */
    val eveningEnd = Color(0xFFA659AE)     // rgb(0.65, 0.35, 0.68)

    /** Deep navy */
    val nightStart = Color(0xFF1A1A38)     // rgb(0.10, 0.10, 0.22)
    /** Dark purple */
    val nightEnd = Color(0xFF2E264D)       // rgb(0.18, 0.15, 0.30)

    // ── Brand ────────────────────────────────────────────────────────────

    /** Indigo brand colour */
    val brand = Color(0xFF6366F2)          // rgb(0.39, 0.40, 0.95)
    val brandLight = Color(0xFF8C8FFF)     // rgb(0.55, 0.56, 1.0)

    // ── Semantic ─────────────────────────────────────────────────────────

    val stopRed = Color(0xFFED4242)        // rgb(0.93, 0.26, 0.26)
    val snoozeAmber = Color(0xFFF59E0A)    // rgb(0.96, 0.62, 0.04)
    val skipOrange = Color(0xFFF59E0A)     // rgb(0.96, 0.62, 0.04)
    val successGreen = Color(0xFF38CC78)   // rgb(0.22, 0.80, 0.47)

    // ── Surface ──────────────────────────────────────────────────────────

    /** Card background (light) with slight transparency */
    val cardBackground = Color(0xD9FFFFFF)             // white @ 85%
    /** Card background (dark) with slight transparency */
    val cardBackgroundDark = Color(0xD91C1C1E)         // systemBackground-dark @ 85%
    /** Skipped-alarm card tint */
    val cardBackgroundSkipped = Color(0x14FF9500)       // orange @ 8%
}
