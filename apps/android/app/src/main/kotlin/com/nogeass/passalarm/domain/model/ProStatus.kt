package com.nogeass.passalarm.domain.model

enum class ProTier { FREE, PRO }
enum class ProPeriod { MONTHLY, YEARLY }
enum class ProSource { STORE, CROWDFUND, MANUAL }

data class ProStatus(
    val tier: ProTier = ProTier.FREE,
    val expiresAt: Long? = null,
    val period: ProPeriod? = null,
    val source: ProSource = ProSource.STORE,
) {
    val isPro: Boolean get() = tier == ProTier.PRO
    val isLifetime: Boolean get() = isPro && source == ProSource.CROWDFUND && expiresAt == null
    companion object {
        val Free = ProStatus()
    }
}

data class ProProduct(
    val id: String,
    val period: ProPeriod,
    val displayPrice: String,
    val pricePerMonth: String?,
    val trialText: String?,
)
