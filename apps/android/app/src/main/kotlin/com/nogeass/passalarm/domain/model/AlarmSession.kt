package com.nogeass.passalarm.domain.model

sealed class AlarmSession {
    data object Idle : AlarmSession()

    data class Ringing(
        val tokenId: Long,
        val planId: Long,
        val totalRings: Int,
        val intervalMin: Int,
        val currentRingIndex: Int = 1,
    ) : AlarmSession() {
        val isComplete: Boolean get() = currentRingIndex > totalRings
        val progressText: String get() = "$currentRingIndex/$totalRings"
    }
}
