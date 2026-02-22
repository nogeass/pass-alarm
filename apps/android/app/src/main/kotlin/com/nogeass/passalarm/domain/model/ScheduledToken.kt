package com.nogeass.passalarm.domain.model

data class ScheduledToken(
    val id: Long = 0,
    val planId: Long = 0,
    val date: String = "",
    val fireAtEpoch: Long = 0,
    val osIdentifier: Int = 0,
    val status: TokenStatus = TokenStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
