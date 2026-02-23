package com.nogeass.passalarm.domain.usecase

import com.nogeass.passalarm.domain.model.SkipException
import com.nogeass.passalarm.domain.model.SkipReason
import com.nogeass.passalarm.domain.repository.SkipExceptionRepository
import javax.inject.Inject

class SkipDateUseCase @Inject constructor(
    private val skipRepository: SkipExceptionRepository,
    private val reschedule: RescheduleNextNUseCase
) {
    suspend fun execute(planId: Long, date: String, reason: SkipReason = SkipReason.MANUAL) {
        skipRepository.save(SkipException(planId = planId, date = date, reason = reason))
        reschedule()
    }

    suspend fun unskip(planId: Long, date: String) {
        skipRepository.deleteByPlanAndDate(planId, date)
        reschedule()
    }
}
