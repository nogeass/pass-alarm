package com.nogeass.passalarm.domain.scheduler

import com.nogeass.passalarm.domain.model.ScheduledToken

interface AlarmScheduler {
    fun scheduleAlarm(token: ScheduledToken)
    fun cancelAlarm(token: ScheduledToken)
    fun cancelAll()
}
