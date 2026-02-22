package com.nogeass.passalarm.domain.repository

import com.nogeass.passalarm.domain.model.Holiday

interface HolidayRepository {
    suspend fun fetchAll(): List<Holiday>
    suspend fun fetchByDateRange(from: String, to: String): List<Holiday>
    suspend fun isHoliday(date: String): Boolean
    suspend fun insertAll(holidays: List<Holiday>)
}
