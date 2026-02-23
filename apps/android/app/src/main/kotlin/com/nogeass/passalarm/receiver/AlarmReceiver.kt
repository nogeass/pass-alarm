package com.nogeass.passalarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nogeass.passalarm.domain.repository.AlarmPlanRepository
import com.nogeass.passalarm.domain.repository.ScheduledTokenRepository
import com.nogeass.passalarm.service.AlarmForegroundService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receives the exact-alarm broadcast from [android.app.AlarmManager] and
 * starts [AlarmForegroundService] with all the information needed for an
 * alarm session (token ID, repeat count, interval).
 *
 * Because we need to look up the plan from the database (to read
 * repeatCount / intervalMin), we use [goAsync] to allow a short coroutine
 * to run before the receiver finishes.
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TOKEN_ID = "extra_token_id"
        const val EXTRA_OS_IDENTIFIER = "extra_os_identifier"
    }

    @Inject lateinit var tokenRepository: ScheduledTokenRepository
    @Inject lateinit var planRepository: AlarmPlanRepository

    override fun onReceive(context: Context, intent: Intent) {
        val tokenId = intent.getLongExtra(EXTRA_TOKEN_ID, -1)
        val osIdentifier = intent.getIntExtra(EXTRA_OS_IDENTIFIER, -1)
        if (tokenId == -1L || osIdentifier == -1) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Look up the token to get planId, then the plan for session params.
                val token = tokenRepository.fetchByOsIdentifier(osIdentifier)
                val plan = token?.let { planRepository.fetchById(it.planId) }

                val repeatCount = plan?.repeatCount ?: 10
                val intervalMin = plan?.intervalMin ?: 5
                val soundId = plan?.soundId ?: "default"

                val serviceIntent = Intent(context, AlarmForegroundService::class.java).apply {
                    putExtra(EXTRA_TOKEN_ID, tokenId)
                    putExtra(EXTRA_OS_IDENTIFIER, osIdentifier)
                    putExtra(AlarmForegroundService.EXTRA_REPEAT_COUNT, repeatCount)
                    putExtra(AlarmForegroundService.EXTRA_INTERVAL_MIN, intervalMin)
                    putExtra(AlarmForegroundService.EXTRA_SOUND_ID, soundId)
                }
                context.startForegroundService(serviceIntent)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
