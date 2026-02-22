package com.nogeass.passalarm.presentation.pro

import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nogeass.passalarm.domain.model.ProPeriod
import com.nogeass.passalarm.presentation.designsystem.*

/**
 * Full-screen Pro purchase overlay matching the iOS ProPurchaseView design.
 *
 * Shown as a dialog-style screen on top of the current content with an
 * Evening [MapBackdrop], entry scale animation, product pills, and
 * purchase / restore actions.
 *
 * @param onPurchased Called after a successful purchase or restore.
 * @param onDismiss   Called when the user taps the close button.
 * @param viewModel   Provided by Hilt; loads products, handles billing.
 */
@Composable
fun ProPurchaseScreen(
    onPurchased: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: ProPurchaseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    // ── Entry animation ──────────────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (visible) 1.0f else 0.8f,
        animationSpec = tween(durationMillis = 350),
        label = "entry_scale",
    )
    val alphaAnim by animateFloatAsState(
        targetValue = if (visible) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 350),
        label = "entry_alpha",
    )
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .scale(scaleAnim)
            .graphicsLayerAlpha(alphaAnim),
    ) {
        // ── Background ───────────────────────────────────────────────────
        MapBackdrop(timeOfDay = TimeOfDay.Evening)

        // ── Close button ─────────────────────────────────────────────────
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .statusBarsPadding()
                .align(Alignment.TopEnd)
                .padding(PassSpacing.md),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "閉じる",
                tint = Color.White.copy(alpha = 0.8f),
            )
        }

        // ── Content ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PassSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.height(PassSpacing.xxl))

            // Sparkle icon
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(56.dp),
            )

            Spacer(modifier = Modifier.height(PassSpacing.md))

            // Title
            Text(
                text = "Pass Pro",
                style = PassTypography.heroTime.copy(fontSize = 36.sp),
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(PassSpacing.sm))

            // Subtitle
            Text(
                text = "アラームを無限に設定しよう",
                style = PassTypography.cardDate,
                color = Color.White.copy(alpha = 0.8f),
            )

            Spacer(modifier = Modifier.height(PassSpacing.xl))

            // ── Feature rows ─────────────────────────────────────────────
            FeatureRow(
                icon = Icons.Default.AllInclusive,
                text = "アラーム無制限",
            )
            Spacer(modifier = Modifier.height(PassSpacing.md))
            FeatureRow(
                icon = Icons.Default.NotificationsActive,
                text = "優先通知",
            )
            Spacer(modifier = Modifier.height(PassSpacing.md))
            FeatureRow(
                icon = Icons.Default.Palette,
                text = "テーマカスタマイズ",
            )

            Spacer(modifier = Modifier.height(PassSpacing.xl))

            // ── Price pills ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PassSpacing.sm),
            ) {
                val monthly = uiState.products.firstOrNull { it.period == ProPeriod.MONTHLY }
                val yearly = uiState.products.firstOrNull { it.period == ProPeriod.YEARLY }

                PricePill(
                    label = "月額",
                    price = monthly?.displayPrice ?: "--",
                    isSelected = uiState.selectedPeriod == ProPeriod.MONTHLY,
                    onClick = { viewModel.selectPeriod(ProPeriod.MONTHLY) },
                    modifier = Modifier.weight(1f),
                )
                PricePill(
                    label = "年額",
                    price = yearly?.displayPrice ?: "--",
                    subtitle = yearly?.pricePerMonth?.let { "$it/月" },
                    isSelected = uiState.selectedPeriod == ProPeriod.YEARLY,
                    onClick = { viewModel.selectPeriod(ProPeriod.YEARLY) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(PassSpacing.lg))

            // ── Purchase button ──────────────────────────────────────────
            PassButton(
                title = "はじめる",
                isEnabled = !uiState.isPurchasing && uiState.products.isNotEmpty(),
                hapticType = PassHapticType.Success,
            ) {
                activity?.let { act ->
                    viewModel.purchase(act) { onPurchased() }
                }
            }

            Spacer(modifier = Modifier.height(PassSpacing.md))

            // ── Restore button ───────────────────────────────────────────
            Text(
                text = "復元する",
                style = PassTypography.cardDate.copy(fontSize = 14.sp),
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) {
                        viewModel.restore { onPurchased() }
                    }
                    .padding(PassSpacing.sm),
            )

            // ── Error message ────────────────────────────────────────────
            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(PassSpacing.sm))
                Text(
                    text = error,
                    style = PassTypography.badgeText,
                    color = PassColors.stopRed,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(PassSpacing.xxl))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Private composables
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Alpha helper that applies alpha via [graphicsLayer] without conflating with
 * the call-site modifier chain.
 */
private fun Modifier.graphicsLayerAlpha(alpha: Float): Modifier =
    graphicsLayer { this.alpha = alpha }

@Composable
private fun FeatureRow(
    icon: ImageVector,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PassSpacing.md),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp),
        )
        Text(
            text = text,
            style = PassTypography.cardDate,
            color = Color.White,
        )
    }
}

@Composable
private fun PricePill(
    label: String,
    price: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
) {
    val borderColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.3f)
    val bgColor = if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(PassSpacing.cardCorner))
            .clickable { onClick() },
        shape = RoundedCornerShape(PassSpacing.cardCorner),
        color = bgColor,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor,
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(
                    horizontal = PassSpacing.md,
                    vertical = PassSpacing.md,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = PassTypography.badgeText,
                color = Color.White.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(PassSpacing.xs))
            Text(
                text = price,
                style = PassTypography.cardDate.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                ),
                color = Color.White,
            )
            subtitle?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it,
                    style = PassTypography.badgeText,
                    color = Color.White.copy(alpha = 0.5f),
                )
            }
        }
    }
}
