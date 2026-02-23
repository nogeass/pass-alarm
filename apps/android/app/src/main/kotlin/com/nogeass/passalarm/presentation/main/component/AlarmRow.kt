package com.nogeass.passalarm.presentation.main.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import com.nogeass.passalarm.domain.model.AlarmPlan
import com.nogeass.passalarm.domain.model.Weekday
import com.nogeass.passalarm.presentation.designsystem.PassColors
import com.nogeass.passalarm.presentation.designsystem.PassHaptics
import com.nogeass.passalarm.presentation.designsystem.PassSpacing
import com.nogeass.passalarm.presentation.designsystem.PassTypography
import com.nogeass.passalarm.presentation.designsystem.rememberHapticView

/**
 * A single alarm row for the alarm list.
 *
 * Displays the time in large type, weekday summary + label below,
 * and a toggle switch on the right side.
 *
 * @param plan      The alarm plan to display.
 * @param onToggle  Called when the toggle is switched.
 * @param onClick   Called when the row is tapped (for editing).
 * @param modifier  Outer modifier.
 */
@Composable
fun AlarmRow(
    plan: AlarmPlan,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val view = rememberHapticView()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (plan.isEnabled) 1f else 0.5f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                PassHaptics.tap(view)
                onClick()
            },
        shape = RoundedCornerShape(PassSpacing.cardCorner),
        color = PassColors.cardBackground,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PassSpacing.md, vertical = PassSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(PassSpacing.xs),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = plan.timeHHmm,
                    style = PassTypography.cardTime,
                    color = Color.White,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PassSpacing.sm),
                ) {
                    Text(
                        text = formatWeekdays(plan.weekdaysMask),
                        style = PassTypography.badgeText,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                    if (plan.label.isNotBlank()) {
                        Text(
                            text = plan.label,
                            style = PassTypography.badgeText,
                            color = PassColors.brandLight,
                        )
                    }
                }
            }

            Switch(
                checked = plan.isEnabled,
                onCheckedChange = { enabled ->
                    PassHaptics.medium(view)
                    onToggle(enabled)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PassColors.brand,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f),
                ),
            )
        }
    }
}

/**
 * Formats a weekday bit-mask into a human-readable Japanese string.
 *
 * - If Mon-Fri: "平日"
 * - If all days: "毎日"
 * - Otherwise: comma-separated JP day labels
 */
fun formatWeekdays(mask: Int): String {
    val days = Weekday.fromMask(mask)
    if (days.isEmpty()) return "なし"

    val weekdaySet = setOf(
        Weekday.MONDAY, Weekday.TUESDAY, Weekday.WEDNESDAY,
        Weekday.THURSDAY, Weekday.FRIDAY,
    )
    val weekendSet = setOf(Weekday.SATURDAY, Weekday.SUNDAY)

    return when {
        days == Weekday.entries.toSet() -> "毎日"
        days == weekdaySet -> "平日"
        days == weekendSet -> "週末"
        else -> days.sortedBy { it.ordinal }.joinToString(" ") { it.label }
    }
}
