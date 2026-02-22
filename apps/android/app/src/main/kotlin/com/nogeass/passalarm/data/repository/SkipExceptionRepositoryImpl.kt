package com.nogeass.passalarm.data.repository

import com.nogeass.passalarm.data.local.dao.SkipExceptionDao
import com.nogeass.passalarm.data.local.entity.SkipExceptionEntity
import com.nogeass.passalarm.domain.model.SkipException
import com.nogeass.passalarm.domain.repository.SkipExceptionRepository
import javax.inject.Inject

class SkipExceptionRepositoryImpl @Inject constructor(
    private val dao: SkipExceptionDao
) : SkipExceptionRepository {
    override suspend fun fetchAll(): List<SkipException> =
        dao.fetchAll().map { it.toDomain() }

    override suspend fun fetchByDateRange(from: String, to: String): List<SkipException> =
        dao.fetchByDateRange(from, to).map { it.toDomain() }

    override suspend fun save(skip: SkipException): Long =
        dao.upsert(SkipExceptionEntity.fromDomain(skip))

    override suspend fun delete(id: Long) = dao.delete(id)

    override suspend fun deleteByDate(date: String) = dao.deleteByDate(date)
}
