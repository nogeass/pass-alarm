package com.nogeass.passalarm.presentation.queue

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nogeass.passalarm.R
import com.nogeass.passalarm.domain.model.Occurrence
import com.nogeass.passalarm.presentation.designsystem.*
import com.nogeass.passalarm.presentation.pro.ProPurchaseScreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Composable
fun QueueScreen(
    navController: NavController,
    viewModel: QueueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        MapBackdrop(timeOfDay = TimeOfDay.Noon)

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.queue_title),
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            if (uiState.queue.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(PassSpacing.md)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = stringResource(R.string.queue_empty),
                            style = PassTypography.cardDate,
                            color = Color.White.copy(alpha = 0.6f)
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
                            .padding(top = PassSpacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "次に鳴る",
                            style = PassTypography.sectionHeader,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "${next.date} ${next.timeHHmm}",
                            style = PassTypography.cardDate,
                            color = Color.White
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = PassSpacing.md,
                        end = PassSpacing.md,
                        top = PassSpacing.sm,
                        bottom = PassSpacing.xl
                    ),
                    verticalArrangement = Arrangement.spacedBy(PassSpacing.sm)
                ) {
                    items(uiState.queue, key = { it.date }) { occurrence ->
                        AlarmCard(
                            occurrence = occurrence,
                            onSkip = {
                                viewModel.skip(occurrence.date)
                                PraiseMessages.randomSkip()?.let { msg ->
                                    toastMessage = msg
                                    showToast = true
                                }
                            },
                            onUnskip = { viewModel.unskip(occurrence.date) }
                        )
                    }

                    // Pro upsell card – shown at the bottom when not subscribed
                    if (!uiState.isPro && uiState.queue.isNotEmpty()) {
                        item(key = "pro_card") {
                            ProCard(
                                onSwipe = { viewModel.showProPurchase() }
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
            onDismiss = { showToast = false }
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
    onUnskip: () -> Unit
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
                                            stiffness = 200f
                                        )
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
                                            stiffness = 200f
                                        )
                                    )
                                    onUnskip()
                                    offsetX.snapTo(0f)
                                }
                                else -> {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = 0.7f,
                                            stiffness = 300f
                                        )
                                    )
                                }
                            }
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount)
                        }
                    }
                )
            }
            .alpha(if (occurrence.isSkipped) 0.6f else 1.0f),
        shape = RoundedCornerShape(PassSpacing.cardCorner),
        color = if (occurrence.isSkipped)
            PassColors.cardBackgroundSkipped
        else
            PassColors.cardBackground
    ) {
        Column(
            modifier = Modifier.padding(PassSpacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(PassSpacing.xs)) {
                    Text(
                        text = formatDate(occurrence.date),
                        style = PassTypography.cardDate
                    )
                    Text(
                        text = occurrence.timeHHmm,
                        style = PassTypography.cardTime
                    )
                }

                if (occurrence.isSkipped) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = PassColors.skipOrange
                    ) {
                        Text(
                            text = "パス済み",
                            style = PassTypography.badgeText,
                            color = Color.White,
                            modifier = Modifier.padding(
                                horizontal = PassSpacing.sm,
                                vertical = PassSpacing.xs
                            )
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.Gray.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "平日",
                            style = PassTypography.badgeText,
                            color = Color.Gray,
                            modifier = Modifier.padding(
                                horizontal = PassSpacing.sm,
                                vertical = PassSpacing.xs
                            )
                        )
                    }
                }
            }

            occurrence.skipReason?.let { reason ->
                Spacer(modifier = Modifier.height(PassSpacing.xs))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PassSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (reason == "祝日") Icons.Default.Flag
                        else Icons.Default.PanTool,
                        contentDescription = null,
                        tint = PassColors.skipOrange,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = reason,
                        style = PassTypography.badgeText,
                        color = PassColors.skipOrange
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
