package com.nogeass.passalarm.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nogeass.passalarm.presentation.home.HomeScreen
import com.nogeass.passalarm.presentation.onboarding.OnboardingScreen
import com.nogeass.passalarm.presentation.queue.QueueScreen
import com.nogeass.passalarm.presentation.settings.SettingsScreen

@Composable
fun PassAlarmNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Queue.route) { QueueScreen(navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
    }
}
