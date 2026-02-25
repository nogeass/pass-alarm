package com.nogeass.passalarm.presentation.redeem

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nogeass.passalarm.presentation.designsystem.MapBackdrop
import com.nogeass.passalarm.presentation.designsystem.PassButton
import com.nogeass.passalarm.presentation.designsystem.PassButtonSize
import com.nogeass.passalarm.presentation.designsystem.PassColors
import com.nogeass.passalarm.presentation.designsystem.PassHapticType
import com.nogeass.passalarm.presentation.designsystem.PassSpacing
import com.nogeass.passalarm.presentation.designsystem.PassTypography
import com.nogeass.passalarm.presentation.designsystem.TimeOfDay

/**
 * Crowdfunding redemption screen.
 *
 * Guides the user through Google sign-in and token redemption to
 * activate their lifetime Pro entitlement.
 */
@Composable
fun RedeemScreen(
    navController: NavController,
    token: String? = null,
    viewModel: RedeemViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    Box(modifier = Modifier.fillMaxSize()) {
        MapBackdrop(timeOfDay = TimeOfDay.Evening)

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = PassSpacing.sm, vertical = PassSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "戻る",
                        tint = Color.White,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "特典を受け取る",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )

                Spacer(modifier = Modifier.weight(1f))

                // Placeholder for symmetry
                Box(modifier = Modifier.size(48.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = PassSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                when (uiState.step) {
                    RedeemStep.Loading -> LoadingContent()
                    RedeemStep.Disabled -> DisabledContent()
                    RedeemStep.NeedsAuth -> NeedsAuthContent(
                        errorMessage = uiState.errorMessage,
                        onGoogleSignIn = {
                            activity?.let { viewModel.signInWithGoogle(it) }
                        },
                    )
                    RedeemStep.ReadyToClaim -> ReadyToClaimContent(
                        token = uiState.token,
                        userEmail = uiState.userEmail,
                        errorMessage = uiState.errorMessage,
                        onTokenChange = viewModel::updateToken,
                        onClaim = viewModel::claimToken,
                    )
                    RedeemStep.Claiming -> ClaimingContent()
                    RedeemStep.Success -> SuccessContent(
                        onDone = { navController.popBackStack() },
                    )
                    RedeemStep.Error -> ErrorContent(
                        errorMessage = uiState.errorMessage,
                        onRetry = viewModel::retry,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LoadingContent() {
    Spacer(modifier = Modifier.height(PassSpacing.xxl))
    CircularProgressIndicator(
        color = Color.White,
        modifier = Modifier.size(48.dp),
    )
    Spacer(modifier = Modifier.height(PassSpacing.md))
    Text(
        text = "読み込み中...",
        style = PassTypography.cardDate,
        color = Color.White.copy(alpha = 0.7f),
    )
}

@Composable
private fun DisabledContent() {
    Spacer(modifier = Modifier.height(PassSpacing.xxl))
    Icon(
        imageVector = Icons.Default.CardGiftcard,
        contentDescription = null,
        tint = Color.White.copy(alpha = 0.5f),
        modifier = Modifier.size(56.dp),
    )
    Spacer(modifier = Modifier.height(PassSpacing.md))
    Text(
        text = "特典の受け取りは現在停止中です",
        style = PassTypography.cardDate,
        color = Color.White.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(PassSpacing.sm))
    Text(
        text = "しばらくしてからお試しください",
        style = PassTypography.badgeText.copy(fontSize = 14.sp),
        color = Color.White.copy(alpha = 0.5f),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun NeedsAuthContent(
    errorMessage: String?,
    onGoogleSignIn: () -> Unit,
) {
    Spacer(modifier = Modifier.height(PassSpacing.xxl))

    Icon(
        imageVector = Icons.Default.CardGiftcard,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(56.dp),
    )

    Spacer(modifier = Modifier.height(PassSpacing.md))

    Text(
        text = "クラファン特典を受け取る",
        style = PassTypography.heroTime.copy(fontSize = 28.sp),
        color = Color.White,
        textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(PassSpacing.sm))

    Text(
        text = "特典を受け取るにはサインインが必要です",
        style = PassTypography.cardDate,
        color = Color.White.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(PassSpacing.xl))

    PassButton(
        title = "Googleでサインイン",
        size = PassButtonSize.Medium,
        color = Color.White.copy(alpha = 0.2f),
        hapticType = PassHapticType.Tap,
        onClick = onGoogleSignIn,
    )

    errorMessage?.let {
        Spacer(modifier = Modifier.height(PassSpacing.md))
        Text(
            text = it,
            style = PassTypography.badgeText,
            color = PassColors.stopRed,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ReadyToClaimContent(
    token: String,
    userEmail: String?,
    errorMessage: String?,
    onTokenChange: (String) -> Unit,
    onClaim: () -> Unit,
) {
    Spacer(modifier = Modifier.height(PassSpacing.xxl))

    Icon(
        imageVector = Icons.Default.CardGiftcard,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(56.dp),
    )

    Spacer(modifier = Modifier.height(PassSpacing.md))

    Text(
        text = "特典コードを入力",
        style = PassTypography.heroTime.copy(fontSize = 28.sp),
        color = Color.White,
        textAlign = TextAlign.Center,
    )

    userEmail?.let {
        Spacer(modifier = Modifier.height(PassSpacing.sm))
        Text(
            text = it,
            style = PassTypography.badgeText.copy(fontSize = 14.sp),
            color = Color.White.copy(alpha = 0.5f),
        )
    }

    Spacer(modifier = Modifier.height(PassSpacing.xl))

    OutlinedTextField(
        value = token,
        onValueChange = onTokenChange,
        placeholder = {
            Text(
                text = "特典コードを入力...",
                color = Color.White.copy(alpha = 0.3f),
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(PassSpacing.cardCorner),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White,
            focusedBorderColor = Color.White.copy(alpha = 0.5f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
        ),
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(PassSpacing.lg))

    PassButton(
        title = "特典を受け取る",
        size = PassButtonSize.Medium,
        color = PassColors.brand,
        isEnabled = token.isNotBlank(),
        hapticType = PassHapticType.Success,
        onClick = onClaim,
    )

    errorMessage?.let {
        Spacer(modifier = Modifier.height(PassSpacing.md))
        Text(
            text = it,
            style = PassTypography.badgeText,
            color = PassColors.stopRed,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ClaimingContent() {
    Spacer(modifier = Modifier.height(PassSpacing.xxl))
    CircularProgressIndicator(
        color = Color.White,
        modifier = Modifier.size(48.dp),
    )
    Spacer(modifier = Modifier.height(PassSpacing.md))
    Text(
        text = "特典を取得中...",
        style = PassTypography.cardDate,
        color = Color.White.copy(alpha = 0.7f),
    )
}

@Composable
private fun SuccessContent(
    onDone: () -> Unit,
) {
    Spacer(modifier = Modifier.height(PassSpacing.xxl))

    Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = null,
        tint = PassColors.successGreen,
        modifier = Modifier.size(72.dp),
    )

    Spacer(modifier = Modifier.height(PassSpacing.md))

    Text(
        text = "Pro 有効（ライフタイム）",
        style = PassTypography.heroTime.copy(fontSize = 28.sp),
        color = Color.White,
        textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(PassSpacing.sm))

    Text(
        text = "クラウドファンディング特典が有効になりました",
        style = PassTypography.cardDate,
        color = Color.White.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(PassSpacing.xl))

    PassButton(
        title = "OK",
        size = PassButtonSize.Medium,
        color = PassColors.successGreen,
        hapticType = PassHapticType.Success,
        onClick = onDone,
    )
}

@Composable
private fun ErrorContent(
    errorMessage: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    Spacer(modifier = Modifier.height(PassSpacing.xxl))

    Icon(
        imageVector = Icons.Default.Error,
        contentDescription = null,
        tint = PassColors.stopRed,
        modifier = Modifier.size(56.dp),
    )

    Spacer(modifier = Modifier.height(PassSpacing.md))

    Text(
        text = "エラーが発生しました",
        style = PassTypography.heroTime.copy(fontSize = 24.sp),
        color = Color.White,
        textAlign = TextAlign.Center,
    )

    errorMessage?.let {
        Spacer(modifier = Modifier.height(PassSpacing.sm))
        Text(
            text = it,
            style = PassTypography.cardDate,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
    }

    Spacer(modifier = Modifier.height(PassSpacing.xl))

    PassButton(
        title = "再試行",
        size = PassButtonSize.Medium,
        color = PassColors.brand,
        hapticType = PassHapticType.Tap,
        onClick = onRetry,
    )

    Spacer(modifier = Modifier.height(PassSpacing.md))

    PassButton(
        title = "戻る",
        size = PassButtonSize.Small,
        color = Color.White.copy(alpha = 0.2f),
        hapticType = PassHapticType.Tap,
        onClick = onBack,
    )
}
