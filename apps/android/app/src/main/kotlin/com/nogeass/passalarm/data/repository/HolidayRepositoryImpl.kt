package com.nogeass.passalarm.data.repository

import com.nogeass.passalarm.data.local.dao.HolidayJpDao
import com.nogeass.passalarm.data.local.entity.HolidayJpEntity
import com.nogeass.passalarm.domain.model.Holiday
import com.nogeass.passalarm.domain.repository.HolidayRepository
import javax.inject.Inject

class HolidayRepositoryImpl @Inject constructor(
    private val dao: HolidayJpDao
) : HolidayRepository {
    override suspend fun fetchAll(): List<Holiday> =
        dao.fetchAll().map { it.toDomain() }

    override suspend fun fetchByDateRange(from: String, to: String): List<Holiday> =
        dao.fetchByDateRange(from, to).map { it.toDomain() }

    override suspend fun isHoliday(date: String): Boolean =
        dao.isHoliday(date)

    override suspend fun insertAll(holidays: List<Holiday>) =
        dao.upsertAll(holidays.map { HolidayJpEntity.fromDomain(it) })
}
