package com.nogeass.passalarm.data.local.dao

import androidx.room.*
import com.nogeass.passalarm.data.local.entity.ScheduledTokenEntity

@Dao
interface ScheduledTokenDao {
    @Query("SELECT * FROM scheduled_token")
    suspend fun fetchAll(): List<ScheduledTokenEntity>

    @Query("SELECT * FROM scheduled_token WHERE status = 'PENDING'")
    suspend fun fetchPending(): List<ScheduledTokenEntity>

    @Query("SELECT * FROM scheduled_token WHERE osIdentifier = :osId LIMIT 1")
    suspend fun fetchByOsIdentifier(osId: Int): ScheduledTokenEntity?

    @Upsert
    suspend fun upsert(entity: ScheduledTokenEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ScheduledTokenEntity>)

    @Query("UPDATE scheduled_token SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM scheduled_token")
    suspend fun deleteAll()

    @Query("DELETE FROM scheduled_token WHERE status = 'PENDING'")
    suspend fun deletePending()
}
