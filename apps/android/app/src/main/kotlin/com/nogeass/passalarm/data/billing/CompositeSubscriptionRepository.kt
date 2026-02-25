package com.nogeass.passalarm.data.billing

import android.app.Activity
import com.nogeass.passalarm.domain.model.ProProduct
import com.nogeass.passalarm.domain.model.ProSource
import com.nogeass.passalarm.domain.model.ProStatus
import com.nogeass.passalarm.domain.model.ProTier
import com.nogeass.passalarm.domain.repository.ServerEntitlementRepository
import com.nogeass.passalarm.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Merges Play Billing subscriptions with server-side crowdfunding/manual
 * entitlements. If either source grants Pro, the user is Pro.
 *
 * Server entitlements take priority for [ProSource] when both are active.
 */
class CompositeSubscriptionRepository @Inject constructor(
    private val playBilling: PlayBillingSubscriptionRepository,
    private val serverEntitlements: ServerEntitlementRepository,
) : SubscriptionRepository {

    override fun observeStatus(): Flow<ProStatus> =
        playBilling.observeStatus().map { playStatus ->
            mergeStatus(playStatus)
        }

    override suspend fun currentStatus(): ProStatus {
        val playStatus = playBilling.currentStatus()
        // Also try to refresh server entitlements if user is signed in
        runCatching { serverEntitlements.fetchEntitlements() }
        return mergeStatus(playStatus)
    }

    override suspend fun fetchProducts(): List<ProProduct> =
        playBilling.fetchProducts()

    override suspend fun purchase(activity: Activity, product: ProProduct): ProStatus {
        val playStatus = playBilling.purchase(activity, product)
        return mergeStatus(playStatus)
    }

    override suspend fun restorePurchases(): ProStatus {
        val playStatus = playBilling.restorePurchases()
        runCatching { serverEntitlements.fetchEntitlements() }
        return mergeStatus(playStatus)
    }

    private fun mergeStatus(playStatus: ProStatus): ProStatus {
        val serverEnts = serverEntitlements.cachedEntitlements()
        val activeServerEnt = serverEnts.firstOrNull { ent ->
            ent.tier == ProTier.PRO &&
                (ent.expiresAt == null || ent.expiresAt > System.currentTimeMillis())
        }

        return when {
            // Server entitlement takes precedence (crowdfund lifetime, etc.)
            activeServerEnt != null -> ProStatus(
                tier = ProTier.PRO,
                expiresAt = activeServerEnt.expiresAt,
                period = playStatus.period, // Keep Play period info if available
                source = activeServerEnt.source,
            )
            // Fall back to Play Billing status
            playStatus.isPro -> playStatus
            // Neither active
            else -> ProStatus.Free
        }
    }
}
