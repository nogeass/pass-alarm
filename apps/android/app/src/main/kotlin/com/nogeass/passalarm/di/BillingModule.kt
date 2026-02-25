package com.nogeass.passalarm.di

import com.nogeass.passalarm.data.billing.CompositeSubscriptionRepository
import com.nogeass.passalarm.data.billing.PlayBillingSubscriptionRepository
import com.nogeass.passalarm.domain.repository.ServerEntitlementRepository
import com.nogeass.passalarm.domain.repository.SubscriptionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {

    @Provides
    @Singleton
    fun provideSubscriptionRepository(
        playBilling: PlayBillingSubscriptionRepository,
        serverEntitlements: ServerEntitlementRepository,
    ): SubscriptionRepository =
        CompositeSubscriptionRepository(playBilling, serverEntitlements)
}
