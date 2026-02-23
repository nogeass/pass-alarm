package com.nogeass.passalarm.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nogeass.passalarm.presentation.designsystem.ContentTab
import com.nogeass.passalarm.presentation.designsystem.MapBackdrop
import com.nogeass.passalarm.presentation.designsystem.ModeToggleFab
import com.nogeass.passalarm.presentation.designsystem.PassColors
import com.nogeass.passalarm.presentation.designsystem.PassHaptics
import com.nogeass.passalarm.presentation.designsystem.PassSpacing
import com.nogeass.passalarm.presentation.designsystem.TimeOfDay
import com.nogeass.passalarm.presentation.designsystem.rememberHapticView
import com.nogeass.passalarm.presentation.navigation.Screen

/**
 * Main shell screen with content panel and 3 floating bottom buttons:
 * settings (left), mode toggle (center), add alarm (right).
 */
@Composable
fun MainScreen(
    navController: NavController,
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val view = rememberHapticView()

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        MapBackdrop(
            timeOfDay = when (uiState.selectedTab) {
                ContentTab.LIST -> TimeOfDay.Morning
                ContentTab.SKIP -> TimeOfDay.Noon
            },
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Large title — left aligned
            Text(
                text = when (uiState.selectedTab) {
                    ContentTab.LIST -> "アラーム一覧"
                    ContentTab.SKIP -> "次のアラーム"
                },
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = PassSpacing.md)
                    .padding(vertical = PassSpacing.sm),
            )

            // Content
            when (uiState.selectedTab) {
                ContentTab.LIST -> AlarmListContent(
                    onEdit = { plan ->
                        navController.navigate(Screen.AlarmEdit.createRoute(plan.id))
                    },
                )

                ContentTab.SKIP -> SkipQueueContent()
            }
        }

        // Bottom floating buttons — tight cluster
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = PassSpacing.xl),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Settings button — left
            Surface(
                onClick = {
                    PassHaptics.tap(view)
                    navController.navigate(Screen.Settings.route)
                },
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.15f),
                shadowElevation = 8.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "設定",
                        tint = Color.White,
                    )
                }
            }

            // Mode toggle — center
            ModeToggleFab(
                selectedTab = uiState.selectedTab,
                onTabSelected = { mainViewModel.selectTab(it) },
            )

            // Add button — right (glass style matching settings)
            Surface(
                onClick = {
                    PassHaptics.tap(view)
                    navController.navigate(Screen.AlarmEdit.createRoute(null))
                },
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.15f),
                shadowElevation = 8.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "アラームを追加",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}
