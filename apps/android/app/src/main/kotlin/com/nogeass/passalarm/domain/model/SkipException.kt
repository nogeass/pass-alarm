package com.nogeass.passalarm.domain.model

data class SkipException(
    val id: Long = 0,
    val planId: Long = 0,
    val date: String = "",
    val reason: SkipReason = SkipReason.MANUAL,
    val createdAt: Long = System.currentTimeMillis()
)

enum class SkipReason {
    MANUAL, HOLIDAY, SYSTEM
}
