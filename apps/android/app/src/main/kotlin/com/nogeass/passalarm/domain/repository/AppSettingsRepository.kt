package com.nogeass.passalarm.domain.repository

import com.nogeass.passalarm.domain.model.AppSettings

interface AppSettingsRepository {
    suspend fun get(): AppSettings
    suspend fun save(settings: AppSettings)
}
