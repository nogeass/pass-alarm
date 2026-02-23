package com.nogeass.passalarm.data.local.dao

import androidx.room.*
import com.nogeass.passalarm.data.local.entity.AlarmPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmPlanDao {
    @Query("SELECT * FROM alarm_plan")
    fun observeAll(): Flow<List<AlarmPlanEntity>>

    @Query("SELECT * FROM alarm_plan")
    suspend fun fetchAll(): List<AlarmPlanEntity>

    @Query("SELECT * FROM alarm_plan WHERE id = :id")
    suspend fun fetchById(id: Long): AlarmPlanEntity?

    @Query("SELECT * FROM alarm_plan WHERE isEnabled = 1")
    suspend fun fetchEnabled(): List<AlarmPlanEntity>

    @Upsert
    suspend fun upsert(entity: AlarmPlanEntity): Long

    @Query("DELETE FROM alarm_plan WHERE id = :id")
    suspend fun delete(id: Long)
}
