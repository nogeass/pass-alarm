package com.nogeass.passalarm.data.repository

import android.content.Context
import com.nogeass.passalarm.domain.model.AppSettings
import com.nogeass.passalarm.domain.repository.AppSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DataStoreAppSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : AppSettingsRepository {
    private val prefs get() = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    override suspend fun get() = AppSettings(
        holidayAutoSkip = prefs.getBoolean("holidayAutoSkip", true),
        tutorialCompleted = prefs.getBoolean("tutorialCompleted", false)
    )

    override suspend fun save(settings: AppSettings) {
        prefs.edit()
            .putBoolean("holidayAutoSkip", settings.holidayAutoSkip)
            .putBoolean("tutorialCompleted", settings.tutorialCompleted)
            .apply()
    }
}
