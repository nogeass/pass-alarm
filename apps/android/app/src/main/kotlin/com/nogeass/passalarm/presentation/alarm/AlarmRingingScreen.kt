package com.nogeass.passalarm.presentation.alarm

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nogeass.passalarm.R
import com.nogeass.passalarm.presentation.designsystem.*
import kotlinx.coroutines.delay

@Composable
fun AlarmRingingScreen(
    onStop: () -> Unit,
    onSnooze: () -> Unit,
    progressText: String? = null
) {
    val view = rememberHapticView()
    var wakeUpTriggered by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

    // Animate the gradient transition from night to morning
    val morningAlpha by animateFloatAsState(
        targetValue = if (wakeUpTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "morningAlpha"
    )

    // Delayed stop after wake animation
    LaunchedEffect(wakeUpTriggered) {
        if (wakeUpTriggered) {
            delay(1_500L)
            onStop()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Night-to-morning gradient transition
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(PassColors.nightStart, PassColors.nightEnd)
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(PassColors.morningStart, PassColors.morningEnd),
                        start = androidx.compose.ui.geometry.Offset.Zero,
                        end = androidx.compose.ui.geometry.Offset(
                            Float.POSITIVE_INFINITY,
                            Float.POSITIVE_INFINITY
                        )
                    ).let { brush ->
                        brush
                    }
                )
                .background(
                    Color.Transparent.copy(alpha = morningAlpha)
                )
        ) {
            // Overlay morning gradient with animated alpha
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                PassColors.morningStart.copy(alpha = morningAlpha),
                                PassColors.morningEnd.copy(alpha = morningAlpha)
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PassSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "\u23F0",
                fontSize = 80.sp
            )

            Spacer(modifier = Modifier.height(PassSpacing.md))

            progressText?.let {
                Text(
                    text = it,
                    style = PassTypography.cardDate,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(PassSpacing.sm))
            }

            Text(
                text = stringResource(R.string.ringing_alarm),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.weight(1f))

            // Stop button (primary, large circle)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = CircleShape,
                        ambientColor = PassColors.stopRed.copy(alpha = 0.5f),
                        spotColor = PassColors.stopRed.copy(alpha = 0.5f)
                    )
                    .clip(CircleShape)
                    .background(PassColors.stopRed)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        PassHaptics.success(view)
                        wakeUpTriggered = true
                        PraiseMessages
                            .randomWakeUp()
                            ?.let { msg ->
                                toastMessage = msg
                                showToast = true
                            }
                    }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(PassSpacing.sm)
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = stringResource(R.string.ringing_stop),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(PassSpacing.lg))

            // Snooze button (secondary)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(PassColors.snoozeAmber.copy(alpha = 0.8f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        PassHaptics.tap(view)
                        onSnooze()
                    }
                    .padding(horizontal = PassSpacing.xl, vertical = PassSpacing.md)
            ) {
                Text(
                    text = stringResource(R.string.ringing_snooze),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        // PraiseToast overlay
        PraiseToast(
            message = toastMessage,
            isVisible = showToast,
            onDismiss = { showToast = false }
        )
    }
}
