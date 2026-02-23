package com.nogeass.passalarm.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nogeass.passalarm.presentation.alarmedit.AlarmEditScreen
import com.nogeass.passalarm.presentation.main.MainScreen
import com.nogeass.passalarm.presentation.onboarding.OnboardingScreen
import com.nogeass.passalarm.presentation.settings.GlobalSettingsScreen

@Composable
fun PassAlarmNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Main.route,
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.Main.route) {
            MainScreen(navController)
        }
        composable(
            route = Screen.AlarmEdit.route,
            arguments = listOf(
                navArgument("planId") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId")
                ?.takeIf { it != "new" }
                ?.toLongOrNull()
            AlarmEditScreen(
                navController = navController,
                planId = planId,
            )
        }
        composable(Screen.Settings.route) {
            GlobalSettingsScreen(navController)
        }
    }
}
