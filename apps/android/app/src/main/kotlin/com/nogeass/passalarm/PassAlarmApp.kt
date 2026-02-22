package com.nogeass.passalarm

import android.app.Application
import com.nogeass.passalarm.notification.NotificationChannelManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PassAlarmApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannelManager.createChannels(this)
    }
}
