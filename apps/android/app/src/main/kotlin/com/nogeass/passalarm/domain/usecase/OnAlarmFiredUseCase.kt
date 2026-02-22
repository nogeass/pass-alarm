package com.nogeass.passalarm.domain.usecase

import com.nogeass.passalarm.domain.model.TokenStatus
import com.nogeass.passalarm.domain.repository.ScheduledTokenRepository
import javax.inject.Inject

class OnAlarmFiredUseCase @Inject constructor(
    private val tokenRepository: ScheduledTokenRepository,
    private val reschedule: RescheduleNextNUseCase
) {
    suspend fun execute(osIdentifier: Int) {
        val token = tokenRepository.fetchByOsIdentifier(osIdentifier) ?: return
        tokenRepository.updateStatus(token.id, TokenStatus.FIRED)
        reschedule()
    }
}
