package com.nogeass.passalarm.domain.usecase

import com.nogeass.passalarm.domain.repository.AlarmPlanRepository
import javax.inject.Inject

class EnablePlanUseCase @Inject constructor(
    private val planRepository: AlarmPlanRepository,
    private val reschedule: RescheduleNextNUseCase
) {
    suspend fun execute(planId: Long, isEnabled: Boolean) {
        val plan = planRepository.fetchById(planId) ?: return
        planRepository.save(plan.copy(isEnabled = isEnabled, updatedAt = System.currentTimeMillis()))
        reschedule()
    }
}
