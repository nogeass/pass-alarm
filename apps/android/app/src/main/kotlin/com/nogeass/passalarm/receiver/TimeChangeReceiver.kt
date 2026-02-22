package com.nogeass.passalarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nogeass.passalarm.domain.usecase.RescheduleNextNUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimeChangeReceiver : BroadcastReceiver() {
    @Inject lateinit var rescheduleNextNUseCase: RescheduleNextNUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_TIME_CHANGED &&
            intent.action != Intent.ACTION_TIMEZONE_CHANGED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                rescheduleNextNUseCase()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
