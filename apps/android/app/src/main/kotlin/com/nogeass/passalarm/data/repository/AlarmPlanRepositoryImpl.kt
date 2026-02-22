package com.nogeass.passalarm.data.repository

import com.nogeass.passalarm.data.local.dao.AlarmPlanDao
import com.nogeass.passalarm.data.local.entity.AlarmPlanEntity
import com.nogeass.passalarm.domain.model.AlarmPlan
import com.nogeass.passalarm.domain.repository.AlarmPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AlarmPlanRepositoryImpl @Inject constructor(
    private val dao: AlarmPlanDao
) : AlarmPlanRepository {
    override fun observeAll(): Flow<List<AlarmPlan>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun fetchAll(): List<AlarmPlan> =
        dao.fetchAll().map { it.toDomain() }

    override suspend fun fetchById(id: Long): AlarmPlan? =
        dao.fetchById(id)?.toDomain()

    override suspend fun save(plan: AlarmPlan): Long =
        dao.upsert(AlarmPlanEntity.fromDomain(plan))

    override suspend fun delete(id: Long) = dao.delete(id)
}
