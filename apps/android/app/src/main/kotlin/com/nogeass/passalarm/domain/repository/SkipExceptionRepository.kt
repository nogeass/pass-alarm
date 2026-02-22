package com.nogeass.passalarm.domain.repository

import com.nogeass.passalarm.domain.model.SkipException

interface SkipExceptionRepository {
    suspend fun fetchAll(): List<SkipException>
    suspend fun fetchByDateRange(from: String, to: String): List<SkipException>
    suspend fun save(skip: SkipException): Long
    suspend fun delete(id: Long)
    suspend fun deleteByDate(date: String)
}
