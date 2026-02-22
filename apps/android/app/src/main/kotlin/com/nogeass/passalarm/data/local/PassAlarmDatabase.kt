package com.nogeass.passalarm.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nogeass.passalarm.data.local.dao.*
import com.nogeass.passalarm.data.local.entity.*

@Database(
    entities = [
        AlarmPlanEntity::class,
        SkipExceptionEntity::class,
        ScheduledTokenEntity::class,
        HolidayJpEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class PassAlarmDatabase : RoomDatabase() {
    abstract fun alarmPlanDao(): AlarmPlanDao
    abstract fun skipExceptionDao(): SkipExceptionDao
    abstract fun scheduledTokenDao(): ScheduledTokenDao
    abstract fun holidayJpDao(): HolidayJpDao
}
