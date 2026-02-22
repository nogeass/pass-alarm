package com.nogeass.passalarm.di

import android.content.Context
import androidx.room.Room
import com.nogeass.passalarm.data.local.PassAlarmDatabase
import com.nogeass.passalarm.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PassAlarmDatabase =
        Room.databaseBuilder(context, PassAlarmDatabase::class.java, "passalarm.db")
            .build()

    @Provides fun provideAlarmPlanDao(db: PassAlarmDatabase): AlarmPlanDao = db.alarmPlanDao()
    @Provides fun provideSkipExceptionDao(db: PassAlarmDatabase): SkipExceptionDao = db.skipExceptionDao()
    @Provides fun provideScheduledTokenDao(db: PassAlarmDatabase): ScheduledTokenDao = db.scheduledTokenDao()
    @Provides fun provideHolidayJpDao(db: PassAlarmDatabase): HolidayJpDao = db.holidayJpDao()
}
