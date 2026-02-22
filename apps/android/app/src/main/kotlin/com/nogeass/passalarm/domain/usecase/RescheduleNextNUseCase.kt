package com.nogeass.passalarm.domain.usecase

import com.nogeass.passalarm.domain.model.ScheduledToken
import com.nogeass.passalarm.domain.model.TokenStatus
import com.nogeass.passalarm.domain.repository.AlarmPlanRepository
import com.nogeass.passalarm.domain.repository.ScheduledTokenRepository
import com.nogeass.passalarm.domain.scheduler.AlarmScheduler
import javax.inject.Inject

class RescheduleNextNUseCase @Inject constructor(
    private val computeQueue: ComputeQueueUseCase,
    private val tokenRepository: ScheduledTokenRepository,
    private val planRepository: AlarmPlanRepository,
    private val scheduler: AlarmScheduler
) {
    companion object {
        const val SCHEDULE_COUNT = 10
    }

    suspend operator fun invoke() {
        // 1. Cancel existing
        val existing = tokenRepository.fetchPending()
        existing.forEach { scheduler.cancelAlarm(it) }
        tokenRepository.deletePending()

        // 2. Compute fresh queue
        val queue = computeQueue.execute()
        val active = queue.filter { !it.isSkipped }.take(SCHEDULE_COUNT)

        // 3. Get plan for planId
        val plans = planRepository.fetchAll()
        val plan = plans.firstOrNull { it.isEnabled } ?: return

        // 4. Schedule new tokens
        val tokens = active.map { occurrence ->
            val osId = occurrence.date.replace("-", "").toInt() * 10000 +
                    occurrence.timeHHmm.replace(":", "").toInt()
            ScheduledToken(
                planId = plan.id,
                date = occurrence.date,
                fireAtEpoch = occurrence.fireAtEpoch,
                osIdentifier = osId,
                status = TokenStatus.PENDING
            )
        }

        tokens.forEach { token ->
            val savedId = tokenRepository.save(token)
            val savedToken = token.copy(id = savedId)
            scheduler.scheduleAlarm(savedToken)
        }
    }
}
