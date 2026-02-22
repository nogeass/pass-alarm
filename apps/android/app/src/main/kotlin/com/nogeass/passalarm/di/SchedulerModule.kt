package com.nogeass.passalarm.di

import android.app.AlarmManager
import android.content.Context
import com.nogeass.passalarm.data.scheduler.AlarmSchedulerAdapterImpl
import com.nogeass.passalarm.domain.scheduler.AlarmScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SchedulerModule {
    @Binds abstract fun bindAlarmScheduler(impl: AlarmSchedulerAdapterImpl): AlarmScheduler

    companion object {
        @Provides
        fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager =
            context.getSystemService(AlarmManager::class.java)
    }
}
