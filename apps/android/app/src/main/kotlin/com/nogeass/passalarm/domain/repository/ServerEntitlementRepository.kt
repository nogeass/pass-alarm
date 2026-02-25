package com.nogeass.passalarm.domain.repository

import com.nogeass.passalarm.domain.model.ProSource
import com.nogeass.passalarm.domain.model.ProTier

data class ServerEntitlement(
    val id: Int,
    val tier: ProTier,
    val source: ProSource,
    val grantedAt: Long,
    val expiresAt: Long?,
) {
    val isLifetime: Boolean get() = source == ProSource.CROWDFUND && expiresAt == null
}

interface ServerEntitlementRepository {
    suspend fun fetchEntitlements(): List<ServerEntitlement>
    fun cachedEntitlements(): List<ServerEntitlement>
    suspend fun claimToken(token: String): ServerEntitlement
    suspend fun isRedeemDisabled(): Boolean
}
