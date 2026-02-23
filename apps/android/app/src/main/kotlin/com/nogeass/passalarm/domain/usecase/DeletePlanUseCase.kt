package com.nogeass.passalarm.domain.usecase

import com.nogeass.passalarm.domain.repository.AlarmPlanRepository
import javax.inject.Inject

class DeletePlanUseCase @Inject constructor(
    private val planRepository: AlarmPlanRepository,
    private val reschedule: RescheduleNextNUseCase
) {
    suspend fun execute(planId: Long) {
        planRepository.delete(planId)
        reschedule()
    }
}
