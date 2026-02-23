package com.nogeass.passalarm.presentation

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.compose.rememberNavController
import com.nogeass.passalarm.presentation.navigation.PassAlarmNavGraph
import com.nogeass.passalarm.presentation.navigation.Screen
import com.nogeass.passalarm.presentation.theme.PassAlarmTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val notificationsEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(this).areNotificationsEnabled()
        } else {
            true // Pre-Android 13: permission is auto-granted
        }

        val startDestination = if (notificationsEnabled) {
            Screen.Main.route
        } else {
            Screen.Onboarding.route
        }

        setContent {
            PassAlarmTheme {
                val navController = rememberNavController()
                PassAlarmNavGraph(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }
    }
}
