package com.nogeass.passalarm.data.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.nogeass.passalarm.domain.model.ScheduledToken
import com.nogeass.passalarm.domain.scheduler.AlarmScheduler
import com.nogeass.passalarm.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlarmSchedulerAdapterImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager
) : AlarmScheduler {

    override fun scheduleAlarm(token: ScheduledToken) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TOKEN_ID, token.id)
            putExtra(AlarmReceiver.EXTRA_OS_IDENTIFIER, token.osIdentifier)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            token.osIdentifier,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            token.fireAtEpoch,
            pendingIntent
        )
    }

    override fun cancelAlarm(token: ScheduledToken) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            token.osIdentifier,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    override fun cancelAll() {
        // Individual cancellation is needed; AlarmManager has no cancelAll
    }
}
