package com.nogeass.passalarm.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.nogeass.passalarm.R

object NotificationChannelManager {
    const val CHANNEL_ALARM_RINGING = "alarm_ringing"
    const val CHANNEL_ALARM_UPCOMING = "alarm_upcoming"
    const val CHANNEL_GENERAL = "general"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(
                    CHANNEL_ALARM_RINGING,
                    context.getString(R.string.channel_alarm_ringing),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    setBypassDnd(true)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                },
                NotificationChannel(
                    CHANNEL_ALARM_UPCOMING,
                    context.getString(R.string.channel_alarm_upcoming),
                    NotificationManager.IMPORTANCE_LOW
                ),
                NotificationChannel(
                    CHANNEL_GENERAL,
                    context.getString(R.string.channel_general),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        )
    }
}
