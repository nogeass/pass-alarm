package com.nogeass.passalarm.presentation.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Queue : Screen("queue")
    data object Settings : Screen("settings")
}
