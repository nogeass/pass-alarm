package com.nogeass.passalarm.presentation.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Button size tokens matching iOS PassButtonSize.
 */
enum class PassButtonSize(
    val height: Dp,
    val textStyle: TextStyle,
    val cornerRadius: Dp,
) {
    Large(
        height = 64.dp,
        textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
        cornerRadius = 32.dp,
    ),
    Medium(
        height = 48.dp,
        textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
        cornerRadius = 24.dp,
    ),
    Small(
        height = 36.dp,
        textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
        cornerRadius = 18.dp,
    ),
}

/**
 * Zenly-style button with press-scale animation, ripple burst, shadow
 * and haptic feedback.
 *
 * Matches the iOS PassButton composable behaviour:
 * - scales to 0.98 while pressed
 * - fires a circular ripple on tap
 * - triggers haptic feedback via [PassHaptics]
 *
 * @param title     Label text.
 * @param modifier  Outer modifier – the button always fills max width.
 * @param size      One of [PassButtonSize] (Large / Medium / Small).
 * @param color     Background fill colour – defaults to [PassColors.brand].
 * @param isEnabled Whether the button is interactive.
 * @param hapticType Which haptic pattern to fire on tap.
 * @param onClick   Callback invoked on tap.
 */
@Composable
fun PassButton(
    title: String,
    modifier: Modifier = Modifier,
    size: PassButtonSize = PassButtonSize.Medium,
    color: Color = PassColors.brand,
    isEnabled: Boolean = true,
    hapticType: PassHapticType = PassHapticType.Tap,
    onClick: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val view = rememberHapticView()

    // ── Animated values ──────────────────────────────────────────────────
    val scale = remember { Animatable(1f) }
    val rippleRadius = remember { Animatable(0f) }
    val rippleAlpha = remember { Animatable(0f) }
    val rippleCenter = remember { androidx.compose.runtime.mutableStateOf(Offset.Zero) }

    val shape = RoundedCornerShape(size.cornerRadius)
    val bgColor = if (isEnabled) color else color.copy(alpha = 0.4f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(size.height)
            .scale(scale.value)
            .shadow(
                elevation = if (scale.value < 1f) 4.dp else 6.dp,
                shape = shape,
                ambientColor = color.copy(alpha = 0.25f),
                spotColor = color.copy(alpha = 0.25f),
            )
            .clip(shape)
            .pointerInput(isEnabled) {
                if (!isEnabled) return@pointerInput
                detectTapGestures(
                    onPress = { offset ->
                        // Press-down: shrink
                        scope.launch {
                            scale.animateTo(
                                0.98f,
                                animationSpec = tween(durationMillis = 100),
                            )
                        }
                        tryAwaitRelease()
                        // Release: spring back
                        scope.launch {
                            scale.animateTo(
                                1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow,
                                ),
                            )
                        }
                    },
                    onTap = { offset ->
                        // Haptic
                        when (hapticType) {
                            PassHapticType.Tap -> PassHaptics.tap(view)
                            PassHapticType.Medium -> PassHaptics.medium(view)
                            PassHapticType.Success -> PassHaptics.success(view)
                        }

                        // Ripple burst
                        rippleCenter.value = offset
                        scope.launch {
                            rippleRadius.snapTo(0f)
                            rippleAlpha.snapTo(0.3f)
                            launch {
                                rippleRadius.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(durationMillis = 400),
                                )
                            }
                            launch {
                                rippleAlpha.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(
                                        durationMillis = 400,
                                        delayMillis = 100,
                                    ),
                                )
                            }
                        }

                        onClick()
                    },
                )
            },
        color = bgColor,
        shape = shape,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(size.height)
                .drawBehind {
                    // Draw ripple circle
                    if (rippleAlpha.value > 0f) {
                        drawCircle(
                            color = Color.White.copy(alpha = rippleAlpha.value),
                            radius = rippleRadius.value * this.size.maxDimension,
                            center = rippleCenter.value,
                        )
                    }
                },
        ) {
            Text(
                text = title,
                style = size.textStyle,
                color = Color.White,
            )
        }
    }
}
