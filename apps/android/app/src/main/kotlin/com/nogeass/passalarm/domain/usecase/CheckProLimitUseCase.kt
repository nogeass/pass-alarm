package com.nogeass.passalarm.domain.usecase

import com.nogeass.passalarm.domain.repository.AlarmPlanRepository
import com.nogeass.passalarm.domain.repository.SubscriptionRepository
import javax.inject.Inject

class CheckProLimitUseCase @Inject constructor(
    private val planRepository: AlarmPlanRepository,
    private val subscriptionRepository: SubscriptionRepository,
) {
    companion object {
        private const val FREE_PLAN_LIMIT = 10
        private const val PRO_PLAN_LIMIT = 100
    }

    suspend fun canCreatePlan(): Boolean {
        val plans = planRepository.fetchAll()
        val status = subscriptionRepository.currentStatus()
        val limit = if (status.isPro) PRO_PLAN_LIMIT else FREE_PLAN_LIMIT
        return plans.size < limit
    }
}
