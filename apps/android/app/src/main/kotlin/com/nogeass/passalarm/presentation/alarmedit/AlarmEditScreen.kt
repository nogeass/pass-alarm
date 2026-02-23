package com.nogeass.passalarm.presentation.alarmedit

import android.media.RingtoneManager
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nogeass.passalarm.domain.model.Weekday
import com.nogeass.passalarm.presentation.designsystem.MapBackdrop
import com.nogeass.passalarm.presentation.designsystem.PassButton
import com.nogeass.passalarm.presentation.designsystem.PassButtonSize
import com.nogeass.passalarm.presentation.designsystem.PassColors
import com.nogeass.passalarm.presentation.designsystem.PassHapticType
import com.nogeass.passalarm.presentation.designsystem.PassHaptics
import com.nogeass.passalarm.presentation.designsystem.PassSpacing
import com.nogeass.passalarm.presentation.designsystem.PassTypography
import com.nogeass.passalarm.presentation.designsystem.PraiseMessages
import com.nogeass.passalarm.presentation.designsystem.PraiseToast
import com.nogeass.passalarm.presentation.designsystem.SystemSoundPicker
import com.nogeass.passalarm.presentation.designsystem.TimeOfDay
import com.nogeass.passalarm.presentation.designsystem.rememberHapticView

/**
 * Full screen for editing or creating an alarm plan.
 *
 * @param navController Navigation controller for back navigation.
 * @param planId        The plan ID to edit, or null for creating a new plan.
 * @param viewModel     Provided by Hilt via SavedStateHandle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditScreen(
    navController: NavController,
    planId: Long? = null,
    viewModel: AlarmEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val view = rememberHapticView()
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var showSoundPicker by remember { mutableStateOf(false) }
    val soundSheetState = rememberModalBottomSheetState()

    // Navigate back after save or delete
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.popBackStack()
        }
    }
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            navController.popBackStack()
        }
    }

    // Parse current time
    val timeParts = uiState.timeHHmm.split(":")
    val currentHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 7
    val currentMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

    val minuteOptions = listOf(0, 10, 20, 30, 40, 50)

    val weekdays = listOf(
        Weekday.MONDAY to "月",
        Weekday.TUESDAY to "火",
        Weekday.WEDNESDAY to "水",
        Weekday.THURSDAY to "木",
        Weekday.FRIDAY to "金",
        Weekday.SATURDAY to "土",
        Weekday.SUNDAY to "日",
    )

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
                    text = if (uiState.isNew) "新しいアラーム" else "アラームを編集",
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
                // Time picker (10-minute intervals)
                Column(verticalArrangement = Arrangement.spacedBy(PassSpacing.sm)) {
                    Text(
                        text = "時間",
                        style = PassTypography.sectionHeader,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                    Surface(
                        shape = RoundedCornerShape(PassSpacing.cardCorner),
                        color = Color.White.copy(alpha = 0.1f),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = PassSpacing.lg, horizontal = PassSpacing.md),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Hour stepper
                            IconButton(onClick = {
                                PassHaptics.tap(view)
                                val newHour = if (currentHour <= 0) 23 else currentHour - 1
                                viewModel.updateTime("%02d:%02d".format(newHour, currentMinute))
                            }) {
                                Text("-", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "%02d".format(currentHour),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = PassSpacing.sm),
                            )
                            IconButton(onClick = {
                                PassHaptics.tap(view)
                                val newHour = if (currentHour >= 23) 0 else currentHour + 1
                                viewModel.updateTime("%02d:%02d".format(newHour, currentMinute))
                            }) {
                                Text("+", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = ":",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = PassSpacing.xs),
                            )

                            // Minute stepper (10-min)
                            IconButton(onClick = {
                                PassHaptics.tap(view)
                                val idx = minuteOptions.indexOf(currentMinute)
                                val newIdx = if (idx <= 0) minuteOptions.lastIndex else idx - 1
                                viewModel.updateTime("%02d:%02d".format(currentHour, minuteOptions[newIdx]))
                            }) {
                                Text("-", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "%02d".format(currentMinute),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = PassSpacing.sm),
                            )
                            IconButton(onClick = {
                                PassHaptics.tap(view)
                                val idx = minuteOptions.indexOf(currentMinute)
                                val newIdx = if (idx >= minuteOptions.lastIndex) 0 else idx + 1
                                viewModel.updateTime("%02d:%02d".format(currentHour, minuteOptions[newIdx]))
                            }) {
                                Text("+", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Label text field
                Column(verticalArrangement = Arrangement.spacedBy(PassSpacing.sm)) {
                    Text(
                        text = "ラベル",
                        style = PassTypography.sectionHeader,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                    OutlinedTextField(
                        value = uiState.label,
                        onValueChange = { viewModel.updateLabel(it) },
                        placeholder = {
                            Text(
                                text = "通勤、ジムなど",
                                color = Color.White.copy(alpha = 0.3f),
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = PassColors.brand,
                            focusedBorderColor = PassColors.brand,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        ),
                        shape = RoundedCornerShape(PassSpacing.cardCorner),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }

                // Weekday selector
                Column(verticalArrangement = Arrangement.spacedBy(PassSpacing.sm)) {
                    Text(
                        text = "繰り返し",
                        style = PassTypography.sectionHeader,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                    Surface(
                        shape = RoundedCornerShape(PassSpacing.cardCorner),
                        color = Color.White.copy(alpha = 0.1f),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(PassSpacing.md),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            weekdays.forEach { (day, label) ->
                                val isSelected = uiState.weekdaysMask and day.bit != 0
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) PassColors.brand
                                            else Color.White.copy(alpha = 0.1f),
                                        )
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                        ) {
                                            PassHaptics.tap(view)
                                            val newMask = if (isSelected) {
                                                uiState.weekdaysMask and day.bit.inv()
                                            } else {
                                                uiState.weekdaysMask or day.bit
                                            }
                                            viewModel.updateWeekdaysMask(newMask)
                                        },
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White
                                        else Color.White.copy(alpha = 0.5f),
                                    )
                                }
                            }
                        }
                    }
                }

                // Sound picker
                Column(verticalArrangement = Arrangement.spacedBy(PassSpacing.sm)) {
                    Text(
                        text = "アラーム音",
                        style = PassTypography.sectionHeader,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                    Surface(
                        shape = RoundedCornerShape(PassSpacing.cardCorner),
                        color = Color.White.copy(alpha = 0.1f),
                        onClick = { showSoundPicker = true },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(PassSpacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = soundDisplayName(uiState.soundId),
                                color = Color.White,
                                fontSize = 16.sp,
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.4f),
                            )
                        }
                    }
                }

                // Error message
                uiState.saveError?.let { error ->
                    Text(
                        text = error,
                        style = PassTypography.badgeText,
                        color = PassColors.stopRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Save button
                PassButton(
                    title = if (uiState.isNew) "作成する" else "保存する",
                    size = PassButtonSize.Large,
                    color = PassColors.brand,
                    hapticType = PassHapticType.Success,
                    onClick = {
                        viewModel.save()
                    },
                )

                // Delete button (only when editing)
                if (!uiState.isNew) {
                    PassButton(
                        title = "このアラームを削除",
                        size = PassButtonSize.Medium,
                        color = PassColors.stopRed,
                        hapticType = PassHapticType.Medium,
                        onClick = {
                            viewModel.delete()
                        },
                    )
                }

                Spacer(modifier = Modifier.height(PassSpacing.xl))
            }
        }

        // Sound picker bottom sheet
        if (showSoundPicker) {
            ModalBottomSheet(
                onDismissRequest = { showSoundPicker = false },
                sheetState = soundSheetState,
                containerColor = Color.Transparent,
            ) {
                SystemSoundPicker(
                    selectedSoundId = uiState.soundId,
                    onSoundSelected = { viewModel.updateSoundId(it) },
                )
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

@Composable
private fun soundDisplayName(soundId: String): String {
    if (soundId == "default") return "デフォルト"
    val context = LocalContext.current
    return remember(soundId) {
        try {
            val uri = android.net.Uri.parse(soundId)
            RingtoneManager.getRingtone(context, uri)?.getTitle(context) ?: "アラーム音"
        } catch (_: Exception) {
            "アラーム音"
        }
    }
}
