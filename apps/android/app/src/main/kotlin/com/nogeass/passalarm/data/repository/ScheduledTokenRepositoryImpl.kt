package com.nogeass.passalarm.data.repository

import com.nogeass.passalarm.data.local.dao.ScheduledTokenDao
import com.nogeass.passalarm.data.local.entity.ScheduledTokenEntity
import com.nogeass.passalarm.domain.model.ScheduledToken
import com.nogeass.passalarm.domain.model.TokenStatus
import com.nogeass.passalarm.domain.repository.ScheduledTokenRepository
import javax.inject.Inject

class ScheduledTokenRepositoryImpl @Inject constructor(
    private val dao: ScheduledTokenDao
) : ScheduledTokenRepository {
    override suspend fun fetchAll(): List<ScheduledToken> =
        dao.fetchAll().map { it.toDomain() }

    override suspend fun fetchPending(): List<ScheduledToken> =
        dao.fetchPending().map { it.toDomain() }

    override suspend fun fetchByOsIdentifier(osId: Int): ScheduledToken? =
        dao.fetchByOsIdentifier(osId)?.toDomain()

    override suspend fun save(token: ScheduledToken): Long =
        dao.upsert(ScheduledTokenEntity.fromDomain(token))

    override suspend fun saveAll(tokens: List<ScheduledToken>) =
        dao.insertAll(tokens.map { ScheduledTokenEntity.fromDomain(it) })

    override suspend fun updateStatus(id: Long, status: TokenStatus) =
        dao.updateStatus(id, status.name)

    override suspend fun deleteAll() = dao.deleteAll()

    override suspend fun deletePending() = dao.deletePending()
}
