package com.nogeass.passalarm.di

import com.nogeass.passalarm.data.auth.FirebaseAuthServiceImpl
import com.nogeass.passalarm.data.repository.ServerEntitlementRepositoryImpl
import com.nogeass.passalarm.domain.repository.ServerEntitlementRepository
import com.nogeass.passalarm.domain.service.AuthService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    abstract fun bindAuthService(impl: FirebaseAuthServiceImpl): AuthService

    @Binds
    abstract fun bindServerEntitlementRepository(
        impl: ServerEntitlementRepositoryImpl,
    ): ServerEntitlementRepository
}
