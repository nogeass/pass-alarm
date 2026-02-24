package com.nogeass.passalarm.presentation.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import android.content.Intent
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.nogeass.passalarm.domain.model.AlarmPlan
import com.nogeass.passalarm.presentation.designsystem.PassColors
import com.nogeass.passalarm.presentation.designsystem.PassSpacing
import com.nogeass.passalarm.presentation.designsystem.PassTypography
import com.nogeass.passalarm.presentation.main.component.AlarmRow

/**
 * The alarm list content panel: a LazyColumn of [AlarmRow] items
 * with swipe-to-dismiss for delete, and an empty state prompt.
 *
 * @param onEdit  Called with the plan to navigate to the edit screen.
 * @param viewModel Provided by Hilt.
 */
@Composable
fun AlarmListContent(
    onEdit: (AlarmPlan) -> Unit,
    viewModel: AlarmListViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Re-check notification permission when returning from Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && uiState.pendingTogglePlanId != null) {
                val enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
                viewModel.onResumePermissionCheck(enabled)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Notification permission dialog
    if (uiState.showPermissionDialog) {
        NotificationPermissionDialog(
            onOpenSettings = {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                context.startActivity(intent)
            },
            onDismiss = { viewModel.dismissPermissionDialog() },
        )
    }

    if (uiState.plans.isEmpty() && !uiState.isLoading) {
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
                    text = "アラームを追加しよう",
                    style = PassTypography.cardDate,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = PassSpacing.md,
                end = PassSpacing.md,
                top = PassSpacing.sm,
                bottom = 100.dp, // Extra bottom padding for the floating pill
            ),
            verticalArrangement = Arrangement.spacedBy(PassSpacing.sm),
        ) {
            items(uiState.plans, key = { it.id }) { plan ->
                SwipeToDismissAlarmRow(
                    plan = plan,
                    onToggle = { enabled ->
                    if (enabled) {
                        val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
                        viewModel.requestToggleOn(plan.id, notificationsEnabled)
                    } else {
                        viewModel.togglePlan(plan.id, false)
                    }
                },
                    onClick = { onEdit(plan) },
                    onDelete = { viewModel.deletePlan(plan.id) },
                )
            }
        }
    }
}

@Composable
internal fun SwipeToDismissAlarmRow(
    plan: AlarmPlan,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> PassColors.stopRed
                    else -> Color.Transparent
                },
                animationSpec = tween(200),
                label = "dismiss_bg",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(PassSpacing.cardCorner))
                    .background(color)
                    .padding(horizontal = PassSpacing.lg),
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    Text(
                        text = "削除",
                        style = PassTypography.buttonLabel,
                        color = Color.White,
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false,
    ) {
        AlarmRow(
            plan = plan,
            onToggle = onToggle,
            onClick = onClick,
        )
    }
}

@Composable
private fun NotificationPermissionDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("通知が必要です") },
        text = { Text("通知がOFFだとアラームが鳴りません。設定アプリで通知を許可してください。") },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text("設定を開く")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        },
    )
}
