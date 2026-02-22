package com.nogeass.passalarm.domain.usecase

import com.nogeass.passalarm.domain.model.AlarmPlan
import com.nogeass.passalarm.domain.repository.AlarmPlanRepository
import javax.inject.Inject

class UpdatePlanUseCase @Inject constructor(
    private val planRepository: AlarmPlanRepository,
    private val reschedule: RescheduleNextNUseCase
) {
    suspend fun execute(plan: AlarmPlan) {
        planRepository.save(plan.copy(updatedAt = System.currentTimeMillis()))
        reschedule()
    }
}
