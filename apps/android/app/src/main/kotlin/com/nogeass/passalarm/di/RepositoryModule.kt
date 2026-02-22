package com.nogeass.passalarm.di

import com.nogeass.passalarm.data.repository.*
import com.nogeass.passalarm.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindAlarmPlanRepository(impl: AlarmPlanRepositoryImpl): AlarmPlanRepository
    @Binds abstract fun bindSkipExceptionRepository(impl: SkipExceptionRepositoryImpl): SkipExceptionRepository
    @Binds abstract fun bindScheduledTokenRepository(impl: ScheduledTokenRepositoryImpl): ScheduledTokenRepository
    @Binds abstract fun bindHolidayRepository(impl: HolidayRepositoryImpl): HolidayRepository
}
