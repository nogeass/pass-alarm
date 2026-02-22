package com.nogeass.passalarm.presentation.onboarding

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nogeass.passalarm.R
import com.nogeass.passalarm.presentation.designsystem.*

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    val view = rememberHapticView()
    var denied by remember { mutableStateOf(false) }
    var requesting by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
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

    // Auto-navigate after toast shows
    LaunchedEffect(showToast) {
        if (showToast) {
            kotlinx.coroutines.delay(1_500L)
            onComplete()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapBackdrop(timeOfDay = TimeOfDay.Morning)

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

            Text(
                text = stringResource(R.string.onboarding_title),
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(PassSpacing.sm))

            Text(
                text = stringResource(R.string.onboarding_subtitle),
                style = PassTypography.cardDate,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.weight(1f))

            if (denied) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(PassSpacing.md)
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_permission_needed),
                        style = PassTypography.cardDate,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = stringResource(R.string.onboarding_permission_denied),
                        style = PassTypography.badgeText,
                        color = Color.White.copy(alpha = 0.6f)
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
                        }
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
                    }
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
