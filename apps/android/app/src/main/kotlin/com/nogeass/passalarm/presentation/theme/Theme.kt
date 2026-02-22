package com.nogeass.passalarm.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.nogeass.passalarm.presentation.designsystem.PassColors

// ── Brand-aligned Material 3 colour schemes ──────────────────────────────

private val PassLightColorScheme = lightColorScheme(
    primary = PassColors.brand,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = PassColors.brandLight,
    secondary = PassColors.snoozeAmber,
    error = PassColors.stopRed,
    background = androidx.compose.ui.graphics.Color.White,
    surface = androidx.compose.ui.graphics.Color.White,
)

private val PassDarkColorScheme = darkColorScheme(
    primary = PassColors.brandLight,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = PassColors.brand,
    secondary = PassColors.snoozeAmber,
    error = PassColors.stopRed,
    background = PassColors.nightStart,
    surface = PassColors.nightEnd,
)

@Composable
fun PassAlarmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        // Allow dynamic colour on Android 12+ only if explicitly opted-in
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> PassDarkColorScheme
        else -> PassLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
