package com.nogeass.passalarm.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nogeass.passalarm.R
import com.nogeass.passalarm.presentation.designsystem.*
import com.nogeass.passalarm.presentation.navigation.Screen

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black.copy(alpha = 0.3f),
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_home)) },
                    selected = true,
                    onClick = {},
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = PassColors.brand.copy(alpha = 0.3f),
                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                        unselectedTextColor = Color.White.copy(alpha = 0.5f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_queue)) },
                    selected = false,
                    onClick = { navController.navigate(Screen.Queue.route) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                        unselectedTextColor = Color.White.copy(alpha = 0.5f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_settings)) },
                    selected = false,
                    onClick = { navController.navigate(Screen.Settings.route) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                        unselectedTextColor = Color.White.copy(alpha = 0.5f)
                    )
                )
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            MapBackdrop(timeOfDay = TimeOfDay.Morning)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = PassSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // Next alarm card
                uiState.nextOccurrence?.let { next ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(PassSpacing.cardCorner),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = PassSpacing.xl, horizontal = PassSpacing.lg),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.home_next_alarm),
                                style = PassTypography.sectionHeader,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(PassSpacing.sm))
                            Text(
                                text = next.timeHHmm,
                                style = PassTypography.heroTime,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(PassSpacing.xs))
                            Text(
                                text = next.date,
                                style = PassTypography.cardDate,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            next.skipReason?.let { reason ->
                                Spacer(modifier = Modifier.height(PassSpacing.sm))
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = PassColors.skipOrange.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = reason,
                                        style = PassTypography.badgeText,
                                        color = PassColors.skipOrange,
                                        modifier = Modifier.padding(
                                            horizontal = PassSpacing.sm,
                                            vertical = PassSpacing.xs
                                        )
                                    )
                                }
                            }
                        }
                    }
                } ?: run {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(PassSpacing.cardCorner),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = stringResource(R.string.home_no_alarm),
                            style = PassTypography.cardDate,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(PassSpacing.xl)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PassSpacing.lg))

                // Alarm toggle
                uiState.plan?.let { plan ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.home_alarm_enabled),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        PassToggle(
                            isOn = plan.isEnabled,
                            onToggle = { viewModel.togglePlan(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PassSpacing.lg))

                // Skip today button
                PassButton(
                    title = stringResource(R.string.home_skip_today),
                    size = PassButtonSize.Large,
                    color = PassColors.skipOrange,
                    isEnabled = uiState.nextOccurrence != null,
                    hapticType = PassHapticType.Medium,
                    onClick = {
                        viewModel.skipToday()
                        PraiseMessages.randomSkip()?.let { msg ->
                            toastMessage = msg
                            showToast = true
                        }
                    }
                )

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
}
