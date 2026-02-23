package com.nogeass.passalarm.domain.model

data class Occurrence(
    val planId: Long,
    val planLabel: String,
    val date: String,
    val timeHHmm: String,
    val fireAtEpoch: Long,
    val isSkipped: Boolean = false,
    val skipReason: String? = null
)
