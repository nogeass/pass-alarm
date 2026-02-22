package com.nogeass.passalarm.domain.usecase

import com.nogeass.passalarm.domain.model.SkipException
import com.nogeass.passalarm.domain.model.SkipReason
import com.nogeass.passalarm.domain.repository.SkipExceptionRepository
import javax.inject.Inject

class SkipDateUseCase @Inject constructor(
    private val skipRepository: SkipExceptionRepository,
    private val reschedule: RescheduleNextNUseCase
) {
    suspend fun execute(date: String, reason: SkipReason = SkipReason.MANUAL) {
        skipRepository.save(SkipException(date = date, reason = reason))
        reschedule()
    }

    suspend fun unskip(date: String) {
        skipRepository.deleteByDate(date)
        reschedule()
    }
}
