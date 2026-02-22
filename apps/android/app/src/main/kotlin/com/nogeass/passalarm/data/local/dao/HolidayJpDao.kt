package com.nogeass.passalarm.data.local.dao

import androidx.room.*
import com.nogeass.passalarm.data.local.entity.HolidayJpEntity

@Dao
interface HolidayJpDao {
    @Query("SELECT * FROM holiday_jp")
    suspend fun fetchAll(): List<HolidayJpEntity>

    @Query("SELECT * FROM holiday_jp WHERE date >= :from AND date <= :to")
    suspend fun fetchByDateRange(from: String, to: String): List<HolidayJpEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM holiday_jp WHERE date = :date)")
    suspend fun isHoliday(date: String): Boolean

    @Upsert
    suspend fun upsertAll(entities: List<HolidayJpEntity>)
}
