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
import com.nogeass.passalarm.presentation.redeem.RedeemScreen
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
                onNavigateToAlarmEdit = {
                    navController.navigate(Screen.AlarmEdit.createRoute())
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
        composable(
            route = Screen.Redeem.route,
            arguments = listOf(
                navArgument("token") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token")
            RedeemScreen(
                navController = navController,
                token = token,
            )
        }
    }
}
