package com.nogeass.passalarm.presentation.designsystem

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * PassAlarm spacing & corner-radius tokens.
 *
 * Mirrors the iOS PassSpacing enum so both platforms use identical values.
 */
object PassSpacing {

    // ── Spacing scale ────────────────────────────────────────────────────

    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp
    val xxl: Dp = 48.dp

    // ── Corner radii ─────────────────────────────────────────────────────

    val cardCorner: Dp = 20.dp
    val buttonCorner: Dp = 16.dp
    val badgeCorner: Dp = 8.dp
}
