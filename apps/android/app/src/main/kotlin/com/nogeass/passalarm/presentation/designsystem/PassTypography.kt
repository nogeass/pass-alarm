package com.nogeass.passalarm.presentation.designsystem

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * PassAlarm typography scale.
 *
 * Maps 1-to-1 to the iOS PassTypography enum.
 * Uses the default sans-serif font family which on most Android devices
 * resolves to a rounded/geometric typeface similar to SF Rounded on iOS.
 */
object PassTypography {

    private val roundedFamily = FontFamily.SansSerif

    /** Giant clock display – 64 sp, bold, rounded */
    val heroTime = TextStyle(
        fontFamily = roundedFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 64.sp,
        letterSpacing = (-1).sp,
    )

    /** Time shown on alarm cards – 28 sp, bold, rounded */
    val cardTime = TextStyle(
        fontFamily = roundedFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = (-0.5).sp,
    )

    /** Date / subtitle on cards – 16 sp, semi-bold */
    val cardDate = TextStyle(
        fontFamily = roundedFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
    )

    /** Button label – 18 sp, bold */
    val buttonLabel = TextStyle(
        fontFamily = roundedFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
    )

    /** Toast overlay text – 16 sp, semi-bold */
    val toastText = TextStyle(
        fontFamily = roundedFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
    )

    /** Small badge/chip text – 12 sp, medium */
    val badgeText = TextStyle(
        fontFamily = roundedFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
    )

    /** Section header – 14 sp, semi-bold */
    val sectionHeader = TextStyle(
        fontFamily = roundedFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
    )
}
