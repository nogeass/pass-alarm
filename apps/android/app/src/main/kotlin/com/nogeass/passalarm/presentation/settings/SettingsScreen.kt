package com.nogeass.passalarm.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nogeass.passalarm.R
import com.nogeass.passalarm.domain.model.Weekday
import com.nogeass.passalarm.presentation.designsystem.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val view = rememberHapticView()
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

    // Time picker state
    val timeParts = uiState.timeHHmm.split(":")
    val timePickerState = rememberTimePickerState(
        initialHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 7,
        initialMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0,
        is24Hour = true
    )

    // Sync time picker changes
    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        val formatted = "%02d:%02d".format(timePickerState.hour, timePickerState.minute)
        if (formatted != uiState.timeHHmm) {
            viewModel.updateTime(formatted)
        }
    }

    val weekdays = listOf(
        Weekday.MONDAY to stringResource(R.string.weekday_mon),
        Weekday.TUESDAY to stringResource(R.string.weekday_tue),
        Weekday.WEDNESDAY to stringResource(R.string.weekday_wed),
        Weekday.THURSDAY to stringResource(R.string.weekday_thu),
        Weekday.FRIDAY to stringResource(R.string.weekday_fri),
        Weekday.SATURDAY to stringResource(R.string.weekday_sat),
        Weekday.SUNDAY to stringResource(R.string.weekday_sun),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        MapBackdrop(timeOfDay = TimeOfDay.Evening)

        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = PassSpacing.md),
                verticalArrangement = Arrangement.spacedBy(PassSpacing.lg)
            ) {
                // Time picker
                Column(verticalArrangement = Arrangement.spacedBy(PassSpacing.sm)) {
                    Text(
                        text = stringResource(R.string.settings_time),
                        style = PassTypography.sectionHeader,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Surface(
                        shape = RoundedCornerShape(PassSpacing.cardCorner),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(PassSpacing.md),
                            contentAlignment = Alignment.Center
                        ) {
                            TimePicker(
                                state = timePickerState,
                                colors = TimePickerDefaults.colors(
                                    clockDialColor = Color.White.copy(alpha = 0.1f),
                                    clockDialSelectedContentColor = Color.White,
                                    clockDialUnselectedContentColor = Color.White.copy(alpha = 0.6f),
                                    selectorColor = PassColors.brand,
                                    containerColor = Color.Transparent,
                                    periodSelectorSelectedContainerColor = PassColors.brand,
                                    periodSelectorSelectedContentColor = Color.White,
                                    timeSelectorSelectedContainerColor = PassColors.brand,
                                    timeSelectorSelectedContentColor = Color.White,
                                    timeSelectorUnselectedContainerColor = Color.White.copy(alpha = 0.1f),
                                    timeSelectorUnselectedContentColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Weekday selector
                Column(verticalArrangement = Arrangement.spacedBy(PassSpacing.sm)) {
                    Text(
                        text = stringResource(R.string.settings_weekdays),
                        style = PassTypography.sectionHeader,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Surface(
                        shape = RoundedCornerShape(PassSpacing.cardCorner),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(PassSpacing.md),
                            horizontalArrangement = Arrangement.SpaceEvenly
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
                                            else Color.White.copy(alpha = 0.1f)
                                        )
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            PassHaptics.tap(view)
                                            val newMask = if (isSelected) {
                                                uiState.weekdaysMask and day.bit.inv()
                                            } else {
                                                uiState.weekdaysMask or day.bit
                                            }
                                            viewModel.updateWeekdaysMask(newMask)
                                        }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White
                                        else Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Repeat settings
                Column(verticalArrangement = Arrangement.spacedBy(PassSpacing.sm)) {
                    Text(
                        text = "連続アラーム",
                        style = PassTypography.sectionHeader,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Surface(
                        shape = RoundedCornerShape(PassSpacing.cardCorner),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(PassSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(PassSpacing.md)
                        ) {
                            // Repeat count
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_repeat_count),
                                    color = Color.White
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(PassSpacing.sm)
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (uiState.repeatCount > 1) {
                                                viewModel.updateRepeatCount(uiState.repeatCount - 1)
                                            }
                                        }
                                    ) {
                                        Text("-", color = Color.White, fontSize = 20.sp)
                                    }
                                    Text(
                                        text = "${uiState.repeatCount}回",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = {
                                            if (uiState.repeatCount < 20) {
                                                viewModel.updateRepeatCount(uiState.repeatCount + 1)
                                            }
                                        }
                                    ) {
                                        Text("+", color = Color.White, fontSize = 20.sp)
                                    }
                                }
                            }

                            // Interval
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_interval),
                                    color = Color.White
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(PassSpacing.sm)
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (uiState.intervalMin > 1) {
                                                viewModel.updateIntervalMin(uiState.intervalMin - 1)
                                            }
                                        }
                                    ) {
                                        Text("-", color = Color.White, fontSize = 20.sp)
                                    }
                                    Text(
                                        text = "${uiState.intervalMin}分",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = {
                                            if (uiState.intervalMin < 30) {
                                                viewModel.updateIntervalMin(uiState.intervalMin + 1)
                                            }
                                        }
                                    ) {
                                        Text("+", color = Color.White, fontSize = 20.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Holiday auto-skip
                Surface(
                    shape = RoundedCornerShape(PassSpacing.cardCorner),
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PassSpacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.settings_holiday_skip),
                                color = Color.White
                            )
                            Text(
                                text = "日本の祝日は自動でパスします",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                        Switch(
                            checked = uiState.holidayAutoSkip,
                            onCheckedChange = { viewModel.updateHolidayAutoSkip(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PassColors.brand,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                    }
                }

                // Save button
                PassButton(
                    title = stringResource(R.string.settings_save),
                    size = PassButtonSize.Large,
                    color = PassColors.brand,
                    hapticType = PassHapticType.Success,
                    onClick = {
                        viewModel.save()
                        toastMessage = PraiseMessages.randomSettingsComplete()
                        showToast = true
                    }
                )

                Spacer(modifier = Modifier.height(PassSpacing.xl))
            }
        }

        // PraiseToast overlay
        PraiseToast(
            message = toastMessage,
            isVisible = showToast,
            onDismiss = { showToast = false }
        )
    }
}
