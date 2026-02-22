package com.nogeass.passalarm.di

import com.nogeass.passalarm.data.billing.PlayBillingSubscriptionRepository
import com.nogeass.passalarm.domain.repository.SubscriptionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BillingModule {
    @Binds
    abstract fun bindSubscriptionRepository(
        impl: PlayBillingSubscriptionRepository,
    ): SubscriptionRepository
}
