package com.nogeass.passalarm.presentation.queue

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nogeass.passalarm.presentation.designsystem.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Shimmer gradient card that promotes Pro upgrade, matching the iOS ProCardView.
 *
 * - Background: horizontal gradient from brand to brandLight
 * - Shimmer: diagonal light band animating across the surface
 * - Breathe: subtle scale oscillation 1.0 <-> 1.01
 * - Swipe: right-only drag; when threshold (300f) is exceeded the card
 *   flies off screen and [onSwipe] fires.
 */
@Composable
fun ProCard(
    onSwipe: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val view = rememberHapticView()
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val threshold = 300f

    // ── Infinite transitions ─────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "pro_card")

    // Shimmer: diagonal band sweeps from left to right
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer",
    )

    // Breathe: gentle scale pulse
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.01f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathe",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .graphicsLayer { rotationZ = offsetX.value / 20f }
            .scale(breatheScale)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            if (offsetX.value > threshold) {
                                PassHaptics.success(view)
                                offsetX.animateTo(
                                    targetValue = 1500f,
                                    animationSpec = spring(
                                        dampingRatio = 0.6f,
                                        stiffness = 200f,
                                    ),
                                )
                                onSwipe()
                                offsetX.snapTo(0f)
                            } else {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = 0.7f,
                                        stiffness = 300f,
                                    ),
                                )
                            }
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        scope.launch {
                            // Right-only: coerce at 0 so the card cannot be dragged left
                            val newValue = (offsetX.value + dragAmount).coerceAtLeast(0f)
                            offsetX.snapTo(newValue)
                        }
                    },
                )
            },
        shape = RoundedCornerShape(PassSpacing.cardCorner),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // ── Base gradient ─────────────────────────────────────
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(PassColors.brand, PassColors.brandLight),
                        ),
                    )

                    // ── Shimmer band ─────────────────────────────────────
                    val bandWidth = size.width * 0.4f
                    val start = size.width * shimmerOffset
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.18f),
                                Color.Transparent,
                            ),
                            start = Offset(start, 0f),
                            end = Offset(start + bandWidth, size.height),
                        ),
                    )
                }
                .padding(PassSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left: sparkle + "Pro"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PassSpacing.sm),
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = "Pro",
                        style = PassTypography.cardDate.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                        ),
                        color = Color.White,
                    )
                }

                // Right: hint + arrow
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PassSpacing.xs),
                ) {
                    Text(
                        text = "スワイプで開く",
                        style = PassTypography.badgeText,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardDoubleArrowRight,
                        contentDescription = "スワイプで開く",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
