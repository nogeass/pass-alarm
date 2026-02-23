package com.nogeass.passalarm.presentation.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Main : Screen("main")
    data object AlarmEdit : Screen("alarm_edit/{planId}") {
        fun createRoute(planId: Long? = null): String =
            "alarm_edit/${planId ?: "new"}"
    }
    data object Settings : Screen("settings")
}
