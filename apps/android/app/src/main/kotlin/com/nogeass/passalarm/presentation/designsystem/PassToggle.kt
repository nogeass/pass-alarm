package com.nogeass.passalarm.presentation.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Zenly-style toggle with a day/night gradient track, a sliding thumb,
 * and a moon icon that appears when the toggle is off.
 *
 * Mirrors the iOS PassToggle view.
 *
 * @param isOn       Current on/off state.
 * @param onToggle   Called with the new value when the user taps.
 * @param onMessage  Brief text shown after toggling **on** (e.g. "OK、明日も起こす").
 * @param offMessage Brief text shown after toggling **off** (e.g. "今日はおやすみモード").
 * @param modifier   Outer modifier.
 */
@Composable
fun PassToggle(
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onMessage: String = "OK、明日も起こす",
    offMessage: String = "今日はおやすみモード",
) {
    val view = rememberHapticView()

    // ── Toggle message state ─────────────────────────────────────────────
    var showMessage by remember { mutableStateOf(false) }
    var displayMessage by remember { mutableStateOf("") }

    // Auto-hide message after 1.5 s
    if (showMessage) {
        LaunchedEffect(displayMessage) {
            delay(1_500L)
            showMessage = false
        }
    }

    // ── Animated thumb offset ────────────────────────────────────────────
    val thumbOffset by animateDpAsState(
        targetValue = if (isOn) 16.dp else (-16).dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "thumb",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        // ── Track ────────────────────────────────────────────────────────
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 72.dp, height = 40.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (isOn) {
                            listOf(PassColors.morningStart, PassColors.morningEnd)
                        } else {
                            listOf(PassColors.nightStart, PassColors.nightEnd)
                        },
                    ),
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    PassHaptics.medium(view)
                    val newValue = !isOn
                    onToggle(newValue)
                    displayMessage = if (newValue) onMessage else offMessage
                    showMessage = true
                },
        ) {
            // Moon icon (visible when off)
            AnimatedVisibility(
                visible = !isOn,
                enter = fadeIn() + scaleIn(initialScale = 0.5f),
                exit = fadeOut() + scaleOut(targetScale = 0.5f),
                modifier = Modifier.offset(x = 16.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.DarkMode,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp),
                )
            }

            // Thumb
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(32.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        ambientColor = Color.Black.copy(alpha = 0.15f),
                        spotColor = Color.Black.copy(alpha = 0.15f),
                    )
                    .clip(CircleShape)
                    .background(Color.White),
            )
        }

        // ── Toggle message ───────────────────────────────────────────────
        Spacer(modifier = Modifier.height(PassSpacing.sm))

        AnimatedVisibility(
            visible = showMessage,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f),
        ) {
            Text(
                text = displayMessage,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = Color.Gray,
            )
        }
    }
}
