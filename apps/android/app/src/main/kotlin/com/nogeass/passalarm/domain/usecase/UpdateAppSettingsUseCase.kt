package com.nogeass.passalarm.domain.usecase

import com.nogeass.passalarm.domain.model.AppSettings
import com.nogeass.passalarm.domain.repository.AppSettingsRepository
import javax.inject.Inject

class UpdateAppSettingsUseCase @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val reschedule: RescheduleNextNUseCase
) {
    suspend fun execute(settings: AppSettings) {
        appSettingsRepository.save(settings)
        reschedule()
    }
}
