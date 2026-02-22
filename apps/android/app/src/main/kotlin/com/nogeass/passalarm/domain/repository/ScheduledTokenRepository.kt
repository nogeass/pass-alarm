package com.nogeass.passalarm.domain.repository

import com.nogeass.passalarm.domain.model.ScheduledToken
import com.nogeass.passalarm.domain.model.TokenStatus

interface ScheduledTokenRepository {
    suspend fun fetchAll(): List<ScheduledToken>
    suspend fun fetchPending(): List<ScheduledToken>
    suspend fun fetchByOsIdentifier(osId: Int): ScheduledToken?
    suspend fun save(token: ScheduledToken): Long
    suspend fun saveAll(tokens: List<ScheduledToken>)
    suspend fun updateStatus(id: Long, status: TokenStatus)
    suspend fun deleteAll()
    suspend fun deletePending()
}
