package com.nogeass.passalarm.presentation.settings

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import com.nogeass.passalarm.presentation.designsystem.PraiseMessages
import com.nogeass.passalarm.presentation.designsystem.PraiseToast
import com.nogeass.passalarm.presentation.designsystem.TimeOfDay
import com.nogeass.passalarm.presentation.pro.ProPurchaseScreen

/**
 * Global settings screen.
 *
 * Displays holiday auto-skip toggle, Pro status / upgrade section,
 * restore purchases button, and app version.
 *
 * @param navController Navigation controller for back navigation.
 * @param viewModel     Provided by Hilt.
 */
@Composable
fun GlobalSettingsScreen(
    navController: NavController,
    viewModel: GlobalSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

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
                    text = "設定",
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
                    .padding(horizontal = PassSpacing.md),
                verticalArrangement = Arrangement.spacedBy(PassSpacing.lg),
            ) {
                // Holiday auto-skip
                Surface(
                    shape = RoundedCornerShape(PassSpacing.cardCorner),
                    color = Color.White.copy(alpha = 0.1f),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PassSpacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "祝日自動スキップ",
                                color = Color.White,
                            )
                            Text(
                                text = "日本の祝日は自動でパスします",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.5f),
                            )
                        }
                        Switch(
                            checked = uiState.holidayAutoSkip,
                            onCheckedChange = { viewModel.toggleHolidayAutoSkip(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PassColors.brand,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.2f),
                            ),
                        )
                    }
                }

                // Pro status section
                Surface(
                    shape = RoundedCornerShape(PassSpacing.cardCorner),
                    color = Color.White.copy(alpha = 0.1f),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PassSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(PassSpacing.md),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(PassSpacing.sm),
                        ) {
                            Icon(
                                imageVector = if (uiState.isPro) Icons.Default.CheckCircle
                                else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (uiState.isPro) PassColors.successGreen else PassColors.brand,
                                modifier = Modifier.size(24.dp),
                            )
                            Text(
                                text = if (uiState.isPro) "Pro メンバー" else "無料プラン",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )
                        }

                        if (!uiState.isPro) {
                            Text(
                                text = "Proにアップグレードしてアラームを無制限に設定しよう",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.6f),
                            )
                            PassButton(
                                title = "Proにアップグレード",
                                size = PassButtonSize.Medium,
                                color = PassColors.brand,
                                hapticType = PassHapticType.Tap,
                                onClick = { viewModel.showProPurchase() },
                            )
                        }
                    }
                }

                // Restore purchases
                Text(
                    text = "購入を復元する",
                    style = PassTypography.cardDate.copy(fontSize = 14.sp),
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) {
                            viewModel.restorePurchases()
                        }
                        .padding(PassSpacing.sm),
                )

                // Restore message
                uiState.restoreMessage?.let { message ->
                    Text(
                        text = message,
                        style = PassTypography.badgeText,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                }

                // Feedback
                Surface(
                    shape = RoundedCornerShape(PassSpacing.cardCorner),
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.clickable {
                        navController.navigate("feedback")
                    },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PassSpacing.md),
                        horizontalArrangement = Arrangement.spacedBy(PassSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "アプリの改善要望を送信する",
                                color = Color.White,
                            )
                            Text(
                                text = "ご意見をお聞かせください",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.5f),
                            )
                        }
                    }
                }

                // App version
                Spacer(modifier = Modifier.height(PassSpacing.lg))
                Text(
                    text = "パスアラーム v2.0",
                    style = PassTypography.badgeText,
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                Spacer(modifier = Modifier.height(PassSpacing.xl))
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
