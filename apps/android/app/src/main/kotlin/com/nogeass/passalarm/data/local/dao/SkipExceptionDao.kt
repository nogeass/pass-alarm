package com.nogeass.passalarm.data.local.dao

import androidx.room.*
import com.nogeass.passalarm.data.local.entity.SkipExceptionEntity

@Dao
interface SkipExceptionDao {
    @Query("SELECT * FROM skip_exception")
    suspend fun fetchAll(): List<SkipExceptionEntity>

    @Query("SELECT * FROM skip_exception WHERE date >= :from AND date <= :to")
    suspend fun fetchByDateRange(from: String, to: String): List<SkipExceptionEntity>

    @Query("SELECT * FROM skip_exception WHERE planId = :planId AND date >= :from AND date <= :to")
    suspend fun fetchByPlanAndDateRange(planId: Long, from: String, to: String): List<SkipExceptionEntity>

    @Upsert
    suspend fun upsert(entity: SkipExceptionEntity): Long

    @Query("DELETE FROM skip_exception WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM skip_exception WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM skip_exception WHERE planId = :planId AND date = :date")
    suspend fun deleteByPlanAndDate(planId: Long, date: String)
}
