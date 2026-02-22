package com.nogeass.passalarm.domain.repository

import android.app.Activity
import com.nogeass.passalarm.domain.model.ProProduct
import com.nogeass.passalarm.domain.model.ProStatus
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun observeStatus(): Flow<ProStatus>
    suspend fun currentStatus(): ProStatus
    suspend fun fetchProducts(): List<ProProduct>
    suspend fun purchase(activity: Activity, product: ProProduct): ProStatus
    suspend fun restorePurchases(): ProStatus
}
