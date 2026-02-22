package com.nogeass.passalarm.domain.model

enum class ProTier { FREE, PRO }
enum class ProPeriod { MONTHLY, YEARLY }

data class ProStatus(
    val tier: ProTier = ProTier.FREE,
    val expiresAt: Long? = null,
    val period: ProPeriod? = null,
) {
    val isPro: Boolean get() = tier == ProTier.PRO
    companion object {
        val Free = ProStatus()
    }
}

data class ProProduct(
    val id: String,
    val period: ProPeriod,
    val displayPrice: String,
    val pricePerMonth: String?,
)
