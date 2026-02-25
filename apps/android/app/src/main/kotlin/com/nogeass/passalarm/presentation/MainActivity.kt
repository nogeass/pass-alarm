package com.nogeass.passalarm.presentation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.nogeass.passalarm.domain.repository.AlarmPlanRepository
import com.nogeass.passalarm.domain.repository.AppSettingsRepository
import com.nogeass.passalarm.presentation.navigation.PassAlarmNavGraph
import com.nogeass.passalarm.presentation.navigation.Screen
import com.nogeass.passalarm.presentation.theme.PassAlarmTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var appSettingsRepository: AppSettingsRepository
    @Inject lateinit var alarmPlanRepository: AlarmPlanRepository

    private var navController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val notificationsEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(this).areNotificationsEnabled()
        } else {
            true
        }

        val tutorialCompleted = runBlocking {
            val settings = appSettingsRepository.get()
            if (!settings.tutorialCompleted) {
                val plans = alarmPlanRepository.fetchAll()
                if (plans.isNotEmpty()) {
                    appSettingsRepository.save(settings.copy(tutorialCompleted = true))
                    true
                } else {
                    false
                }
            } else {
                true
            }
        }

        val startDestination = if (tutorialCompleted) {
            Screen.Main.route
        } else {
            Screen.Onboarding.route
        }

        setContent {
            PassAlarmTheme {
                val nav = rememberNavController()
                navController = nav
                PassAlarmNavGraph(
                    navController = nav,
                    startDestination = startDestination
                )
            }
        }

        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.host == "pass-alarm.nogeass.com" && uri.pathSegments.size >= 2 && uri.pathSegments[0] == "r") {
            val token = uri.pathSegments[1]
            navController?.navigate(Screen.Redeem.createRoute(token))
        }
    }
}
