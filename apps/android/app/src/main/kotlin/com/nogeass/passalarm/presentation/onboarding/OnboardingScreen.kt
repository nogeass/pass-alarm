package com.nogeass.passalarm.presentation.onboarding

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nogeass.passalarm.R
import com.nogeass.passalarm.presentation.designsystem.MapBackdrop
import com.nogeass.passalarm.presentation.designsystem.PassButton
import com.nogeass.passalarm.presentation.designsystem.PassButtonSize
import com.nogeass.passalarm.presentation.designsystem.PassColors
import com.nogeass.passalarm.presentation.designsystem.PassHapticType
import com.nogeass.passalarm.presentation.designsystem.PassHaptics
import com.nogeass.passalarm.presentation.designsystem.PassSpacing
import com.nogeass.passalarm.presentation.designsystem.PassTypography
import com.nogeass.passalarm.presentation.designsystem.PraiseToast
import com.nogeass.passalarm.presentation.designsystem.TimeOfDay
import com.nogeass.passalarm.presentation.designsystem.rememberHapticView
import com.nogeass.passalarm.presentation.main.AlarmCard
import com.nogeass.passalarm.presentation.main.SwipeToDismissAlarmRow
import kotlinx.coroutines.delay

/**
 * Multi-step onboarding:
 * Step 0 – Permission request
 * Steps 1-3 – Interactive tutorial (create alarm, skip, delete)
 */
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onNavigateToAlarmEdit: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val view = rememberHapticView()
    var denied by remember { mutableStateOf(false) }
    var requesting by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(0) }

    val tutorialState by viewModel.tutorialState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        requesting = false
        if (granted) {
            toastMessage = "いいね これで起こせる"
            showToast = true
        } else {
            denied = true
            PassHaptics.warning(view)
        }
    }

    // After permission toast, move to tutorial step 1
    LaunchedEffect(showToast, step) {
        if (showToast && step == 0) {
            delay(1_500L)
            showToast = false
            step = 1
            viewModel.inferStep()
        }
    }

    // Sync tutorial step to local step
    LaunchedEffect(tutorialState.currentStep) {
        if (step >= 1) {
            step = when (tutorialState.currentStep) {
                TutorialStep.CreateAlarm -> 1
                TutorialStep.SkipAlarm -> 2
                TutorialStep.DeleteAlarm -> 3
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapBackdrop(timeOfDay = TimeOfDay.Morning)

        when (step) {
            0 -> PermissionStep(
                denied = denied,
                requesting = requesting,
                onRequestPermission = {
                    requesting = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        toastMessage = "いいね これで起こせる"
                        showToast = true
                    }
                },
                onOpenSettings = {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                },
                onSkip = {
                    step = 1
                    viewModel.inferStep()
                },
            )

            1 -> TutorialCreateAlarmStep(
                onCreateAlarm = onNavigateToAlarmEdit,
            )

            2 -> TutorialSkipStep(
                tutorialState = tutorialState,
                onSkip = { planId, date ->
                    viewModel.skipOccurrence(planId, date)
                    toastMessage = "スキップできた！"
                    showToast = true
                },
                onStepComplete = {
                    viewModel.advanceTo(TutorialStep.DeleteAlarm)
                },
            )

            3 -> TutorialDeleteStep(
                tutorialState = tutorialState,
                onDelete = { planId ->
                    viewModel.deletePlan(planId)
                    viewModel.completeTutorial {
                        toastMessage = "準備完了！"
                        showToast = true
                    }
                },
                onComplete = onComplete,
            )
        }

        // PraiseToast overlay
        PraiseToast(
            message = toastMessage,
            isVisible = showToast,
            onDismiss = { showToast = false },
        )
    }

    // When returning from alarm edit, check if alarm was created
    LaunchedEffect(step) {
        if (step == 1) {
            viewModel.loadPlans()
        }
    }

    // Auto-advance from step 1 if plans exist
    LaunchedEffect(tutorialState.plans) {
        if (step == 1 && tutorialState.plans.isNotEmpty()) {
            toastMessage = "いいね！"
            showToast = true
            delay(1_000L)
            showToast = false
            viewModel.advanceTo(TutorialStep.SkipAlarm)
        }
    }
}

/**
 * Coaching banner displayed at the top of tutorial steps.
 */
@Composable
private fun TutorialBanner(
    stepNumber: Int,
    totalSteps: Int,
    title: String,
    hint: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.1f))
            .padding(vertical = PassSpacing.lg, horizontal = PassSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PassSpacing.sm),
    ) {
        Text(
            text = "$stepNumber/$totalSteps",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.6f),
        )
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Text(
            text = hint,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.6f),
        )
    }
}

// MARK: - Permission Step

@Composable
private fun PermissionStep(
    denied: Boolean,
    requesting: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onSkip: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PassSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(text = "\u23F0", fontSize = 80.sp)

        Spacer(modifier = Modifier.height(PassSpacing.md))

        Text(
            text = stringResource(R.string.onboarding_title),
            fontSize = 34.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
        )

        Spacer(modifier = Modifier.height(PassSpacing.sm))

        Text(
            text = stringResource(R.string.onboarding_subtitle),
            style = PassTypography.cardDate,
            color = Color.White.copy(alpha = 0.7f),
        )

        Spacer(modifier = Modifier.weight(1f))

        if (denied) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(PassSpacing.md),
            ) {
                Text(
                    text = "通知がOFFだとアラームが鳴りません",
                    style = PassTypography.cardDate,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = "あとから設定アプリで許可できます",
                    style = PassTypography.badgeText,
                    color = Color.White.copy(alpha = 0.6f),
                )
                PassButton(
                    title = stringResource(R.string.onboarding_open_settings),
                    size = PassButtonSize.Medium,
                    color = PassColors.brand,
                    onClick = onOpenSettings,
                )
                androidx.compose.material3.TextButton(onClick = onSkip) {
                    Text(
                        text = "あとで設定する",
                        style = PassTypography.badgeText,
                        color = Color.White.copy(alpha = 0.5f),
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(PassSpacing.md),
            ) {
                PassButton(
                    title = stringResource(R.string.onboarding_grant_permission),
                    size = PassButtonSize.Large,
                    color = PassColors.brand,
                    isEnabled = !requesting,
                    hapticType = PassHapticType.Success,
                    onClick = onRequestPermission,
                )
                androidx.compose.material3.TextButton(onClick = onSkip) {
                    Text(
                        text = "あとで設定する",
                        style = PassTypography.badgeText,
                        color = Color.White.copy(alpha = 0.5f),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

// MARK: - Step 1: Create Alarm

@Composable
private fun TutorialCreateAlarmStep(
    onCreateAlarm: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TutorialBanner(
            stepNumber = 1,
            totalSteps = 3,
            title = "アラームをセットしてみましょう",
            hint = "下のボタンをタップしてアラームを作成",
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = PassSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Alarm,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(64.dp),
            )

            Spacer(modifier = Modifier.height(PassSpacing.lg))

            PassButton(
                title = "アラームを作成",
                size = PassButtonSize.Large,
                color = PassColors.brand,
                hapticType = PassHapticType.Success,
                onClick = onCreateAlarm,
            )
        }
    }
}

// MARK: - Step 2: Skip Alarm

@Composable
private fun TutorialSkipStep(
    tutorialState: TutorialUiState,
    onSkip: (Long, String) -> Unit,
    onStepComplete: () -> Unit,
) {
    // Auto-advance after skip
    LaunchedEffect(tutorialState.queue) {
        val hasSkipped = tutorialState.queue.any { it.isSkipped }
        if (hasSkipped) {
            delay(1_000L)
            onStepComplete()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TutorialBanner(
            stepNumber = 2,
            totalSteps = 3,
            title = "スキップしてみましょう",
            hint = "右にスワイプしてアラームをパス",
        )

        if (tutorialState.queue.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "読み込み中...",
                    style = PassTypography.cardDate,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
        } else {
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
                items(tutorialState.queue, key = { "${it.planId}_${it.date}" }) { occurrence ->
                    AlarmCard(
                        occurrence = occurrence,
                        onSkip = { onSkip(occurrence.planId, occurrence.date) },
                        onUnskip = { /* no unskip in tutorial */ },
                    )
                }
            }
        }
    }
}

// MARK: - Step 3: Delete Alarm

@Composable
private fun TutorialDeleteStep(
    tutorialState: TutorialUiState,
    onDelete: (Long) -> Unit,
    onComplete: () -> Unit,
) {
    // Auto-complete after delete
    LaunchedEffect(tutorialState.plans) {
        if (tutorialState.plans.isEmpty() && !tutorialState.isLoading) {
            delay(1_500L)
            onComplete()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TutorialBanner(
            stepNumber = 3,
            totalSteps = 3,
            title = "不要なアラームは削除できます",
            hint = "左にスワイプして削除",
        )

        if (tutorialState.plans.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "読み込み中...",
                    style = PassTypography.cardDate,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
        } else {
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
                items(tutorialState.plans, key = { it.id }) { plan ->
                    SwipeToDismissAlarmRow(
                        plan = plan,
                        onToggle = { /* no toggle in tutorial */ },
                        onClick = { /* no edit in tutorial */ },
                        onDelete = { onDelete(plan.id) },
                    )
                }
            }
        }
    }
}
