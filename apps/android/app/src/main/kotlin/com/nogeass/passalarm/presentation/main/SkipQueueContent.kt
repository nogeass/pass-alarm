package com.nogeass.passalarm.presentation.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nogeass.passalarm.domain.model.Occurrence
import com.nogeass.passalarm.presentation.designsystem.PassColors
import com.nogeass.passalarm.presentation.designsystem.PassHaptics
import com.nogeass.passalarm.presentation.designsystem.PassSpacing
import com.nogeass.passalarm.presentation.designsystem.PassTypography
import com.nogeass.passalarm.presentation.designsystem.PraiseMessages
import com.nogeass.passalarm.presentation.designsystem.PraiseToast
import com.nogeass.passalarm.presentation.designsystem.rememberHapticView
import com.nogeass.passalarm.presentation.pro.ProPurchaseScreen
import com.nogeass.passalarm.presentation.queue.ProCard
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Skip queue content panel: shows the upcoming alarm occurrences
 * with swipe-to-skip cards and Pro upsell.
 *
 * @param viewModel Provided by Hilt.
 */
@Composable
fun SkipQueueContent(
    viewModel: SkipQueueViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (uiState.queue.isEmpty() && !uiState.isLoading) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(PassSpacing.md),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp),
                        )
                        Text(
                            text = "予定されたアラームはありません",
                            style = PassTypography.cardDate,
                            color = Color.White.copy(alpha = 0.6f),
                        )
                    }
                }
            } else {
                // Next alarm header
                uiState.queue.firstOrNull { !it.isSkipped }?.let { next ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PassSpacing.md)
                            .padding(top = PassSpacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "次に鳴る",
                            style = PassTypography.sectionHeader,
                            color = Color.White.copy(alpha = 0.6f),
                        )
                        Text(
                            text = "${formatDate(next.date)} ${next.timeHHmm}",
                            style = PassTypography.cardDate,
                            color = Color.White,
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = PassSpacing.md,
                        end = PassSpacing.md,
                        top = PassSpacing.sm,
                        bottom = 100.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(PassSpacing.sm),
                ) {
                    items(uiState.queue, key = { "${it.planId}_${it.date}" }) { occurrence ->
                        AlarmCard(
                            occurrence = occurrence,
                            onSkip = {
                                viewModel.skip(occurrence.planId, occurrence.date)
                                PraiseMessages.randomSkip()?.let { msg ->
                                    toastMessage = msg
                                    showToast = true
                                }
                            },
                            onUnskip = {
                                viewModel.unskip(occurrence.planId, occurrence.date)
                            },
                        )
                    }

                    // Pro upsell card
                    if (!uiState.isPro && uiState.queue.isNotEmpty()) {
                        item(key = "pro_card") {
                            ProCard(
                                onSwipe = { viewModel.showProPurchase() },
                            )
                        }
                    }
                }
            }
        }

        // PraiseToast overlay
        PraiseToast(
            message = toastMessage,
            isVisible = showToast,
            onDismiss = { showToast = false },
        )

        // Pro purchase full-screen overlay
        if (uiState.showProPurchase) {
            ProPurchaseScreen(
                onPurchased = {
                    viewModel.dismissProPurchase()
                    toastMessage = PraiseMessages.randomPurchase()
                    showToast = true
                },
                onDismiss = { viewModel.dismissProPurchase() },
            )
        }
    }
}

/**
 * Tinder-style swipe card for alarm queue items.
 * Swipe right to skip, swipe left to unskip.
 */
@Composable
private fun AlarmCard(
    occurrence: Occurrence,
    onSkip: () -> Unit,
    onUnskip: () -> Unit,
) {
    val view = rememberHapticView()
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val threshold = 300f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .graphicsLayer {
                rotationZ = offsetX.value / 20f
            }
            .pointerInput(occurrence.isSkipped) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            when {
                                offsetX.value > threshold && !occurrence.isSkipped -> {
                                    PassHaptics.medium(view)
                                    offsetX.animateTo(
                                        targetValue = 1500f,
                                        animationSpec = spring(
                                            dampingRatio = 0.6f,
                                            stiffness = 200f,
                                        ),
                                    )
                                    onSkip()
                                    offsetX.snapTo(0f)
                                }

                                offsetX.value < -threshold && occurrence.isSkipped -> {
                                    PassHaptics.tap(view)
                                    offsetX.animateTo(
                                        targetValue = -1500f,
                                        animationSpec = spring(
                                            dampingRatio = 0.6f,
                                            stiffness = 200f,
                                        ),
                                    )
                                    onUnskip()
                                    offsetX.snapTo(0f)
                                }

                                else -> {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = 0.7f,
                                            stiffness = 300f,
                                        ),
                                    )
                                }
                            }
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount)
                        }
                    },
                )
            }
            .alpha(if (occurrence.isSkipped) 0.6f else 1.0f),
        shape = RoundedCornerShape(PassSpacing.cardCorner),
        color = if (occurrence.isSkipped)
            PassColors.cardBackgroundSkipped
        else
            PassColors.cardBackground,
    ) {
        Column(
            modifier = Modifier.padding(PassSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(PassSpacing.xs)) {
                    if (occurrence.planLabel.isNotBlank()) {
                        Text(
                            text = occurrence.planLabel,
                            style = PassTypography.badgeText,
                            color = PassColors.brandLight,
                        )
                    }
                    Text(
                        text = occurrence.timeHHmm,
                        style = PassTypography.cardTime,
                        color = Color.White,
                    )
                    Text(
                        text = formatDate(occurrence.date),
                        style = PassTypography.cardDate,
                        color = Color.White,
                    )
                }

                if (occurrence.isSkipped) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = PassColors.skipOrange,
                    ) {
                        Text(
                            text = "パス済み",
                            style = PassTypography.badgeText,
                            color = Color.White,
                            modifier = Modifier.padding(
                                horizontal = PassSpacing.sm,
                                vertical = PassSpacing.xs,
                            ),
                        )
                    }
                }
            }

            occurrence.skipReason?.let { reason ->
                Spacer(modifier = Modifier.height(PassSpacing.xs))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PassSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (reason == "祝日") Icons.Default.Flag
                        else Icons.Default.PanTool,
                        contentDescription = null,
                        tint = PassColors.skipOrange,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = reason,
                        style = PassTypography.badgeText,
                        color = PassColors.skipOrange,
                    )
                }
            }
        }
    }
}

private fun formatDate(dateStr: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = parser.parse(dateStr) ?: return dateStr
        val formatter = SimpleDateFormat("M/d (EEE)", Locale.JAPAN)
        formatter.format(date)
    } catch (_: Exception) {
        dateStr
    }
}
