package com.nogeass.passalarm.domain.usecase

import com.nogeass.passalarm.domain.model.ScheduledToken
import com.nogeass.passalarm.domain.model.TokenStatus
import com.nogeass.passalarm.domain.repository.ScheduledTokenRepository
import com.nogeass.passalarm.domain.scheduler.AlarmScheduler
import javax.inject.Inject

class RescheduleNextNUseCase @Inject constructor(
    private val computeQueue: ComputeQueueUseCase,
    private val tokenRepository: ScheduledTokenRepository,
    private val scheduler: AlarmScheduler
) {
    companion object {
        const val MAX_TOKENS = 60
    }

    suspend operator fun invoke() {
        // 1. Cancel existing
        val existing = tokenRepository.fetchPending()
        existing.forEach { scheduler.cancelAlarm(it) }
        tokenRepository.deletePending()

        // 2. Compute fresh merged queue from all enabled plans
        val queue = computeQueue.execute()
        val active = queue.filter { !it.isSkipped }.take(MAX_TOKENS)

        // 3. Schedule new tokens
        val tokens = active.map { occurrence ->
            val osId = occurrence.planId.toInt() * 100_000_000 +
                    occurrence.date.replace("-", "").toInt() % 100_000 * 1000 +
                    occurrence.timeHHmm.replace(":", "").toInt()
            ScheduledToken(
                planId = occurrence.planId,
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
