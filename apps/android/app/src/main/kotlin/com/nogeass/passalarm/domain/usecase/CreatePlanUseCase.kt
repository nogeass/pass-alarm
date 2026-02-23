package com.nogeass.passalarm.domain.usecase

import com.nogeass.passalarm.domain.model.AlarmPlan
import com.nogeass.passalarm.domain.repository.AlarmPlanRepository
import javax.inject.Inject

class CreatePlanUseCase @Inject constructor(
    private val planRepository: AlarmPlanRepository,
    private val checkProLimit: CheckProLimitUseCase,
    private val reschedule: RescheduleNextNUseCase
) {
    suspend fun execute(plan: AlarmPlan): Boolean {
        if (!checkProLimit.canCreatePlan()) return false
        planRepository.save(plan)
        reschedule()
        return true
    }
}
