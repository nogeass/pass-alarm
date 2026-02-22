package com.nogeass.passalarm.domain.repository

import com.nogeass.passalarm.domain.model.AlarmPlan
import kotlinx.coroutines.flow.Flow

interface AlarmPlanRepository {
    fun observeAll(): Flow<List<AlarmPlan>>
    suspend fun fetchAll(): List<AlarmPlan>
    suspend fun fetchById(id: Long): AlarmPlan?
    suspend fun save(plan: AlarmPlan): Long
    suspend fun delete(id: Long)
}
