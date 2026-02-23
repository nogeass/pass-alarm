package com.nogeass.passalarm.presentation.onboarding

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

/**
 * Multi-step onboarding:
 * Step 0 – Permission request
 * Step 1 – Seed default alarms + tutorial hint
 */
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val view = rememberHapticView()
    var denied by remember { mutableStateOf(false) }
    var requesting by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        requesting = false
        if (granted) {
            toastMessage = "いいね これで起こせる"
            showToast = true
            // Move to step 1 after toast
        } else {
            denied = true
            PassHaptics.warning(view)
        }
    }

    // After toast shows on step 0, seed alarms and move to step 1
    LaunchedEffect(showToast, step) {
        if (showToast && step == 0) {
            kotlinx.coroutines.delay(1_500L)
            viewModel.seedDefaultAlarms()
            step = 1
            showToast = false
        }
    }

    // Animated arrow for tutorial
    val infiniteTransition = rememberInfiniteTransition(label = "tutorial_arrow")
    val arrowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "arrow_bounce",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        MapBackdrop(timeOfDay = TimeOfDay.Morning)

        when (step) {
            0 -> {
                // Permission step
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(PassSpacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "\u23F0",
                        fontSize = 80.sp,
                    )

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
                                text = stringResource(R.string.onboarding_permission_needed),
                                style = PassTypography.cardDate,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Text(
                                text = stringResource(R.string.onboarding_permission_denied),
                                style = PassTypography.badgeText,
                                color = Color.White.copy(alpha = 0.6f),
                            )
                            PassButton(
                                title = stringResource(R.string.onboarding_open_settings),
                                size = PassButtonSize.Medium,
                                color = PassColors.brand,
                                onClick = {
                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                    context.startActivity(intent)
                                },
                            )
                        }
                    } else {
                        PassButton(
                            title = stringResource(R.string.onboarding_grant_permission),
                            size = PassButtonSize.Large,
                            color = PassColors.brand,
                            isEnabled = !requesting,
                            hapticType = PassHapticType.Success,
                            onClick = {
                                requesting = true
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    // Pre-13 doesn't need runtime permission
                                    toastMessage = "いいね これで起こせる"
                                    showToast = true
                                }
                            },
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            1 -> {
                // Tutorial step
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(PassSpacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "準備OK",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                    )

                    Spacer(modifier = Modifier.height(PassSpacing.md))

                    Text(
                        text = "3つのアラームを設定したよ",
                        style = PassTypography.cardDate,
                        color = Color.White.copy(alpha = 0.8f),
                    )

                    Spacer(modifier = Modifier.height(PassSpacing.xl))

                    // Animated swipe hint
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "スワイプしてスキップしてみよう",
                            style = PassTypography.cardDate,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                        Spacer(modifier = Modifier.height(PassSpacing.sm))
                        Text(
                            text = "👉",
                            fontSize = 32.sp,
                            modifier = Modifier.offset(x = arrowOffset.dp),
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    PassButton(
                        title = "はじめる",
                        size = PassButtonSize.Large,
                        color = PassColors.brand,
                        hapticType = PassHapticType.Success,
                        onClick = { onComplete() },
                    )

                    Spacer(modifier = Modifier.weight(0.5f))
                }
            }
        }

        // PraiseToast overlay
        PraiseToast(
            message = toastMessage,
            isVisible = showToast,
            onDismiss = { showToast = false },
        )
    }
}
