package com.nogeass.passalarm.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nogeass.passalarm.domain.model.SkipException
import com.nogeass.passalarm.domain.model.SkipReason

@Entity(tableName = "skip_exception", indices = [Index(value = ["date"])])
data class SkipExceptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val reason: String,
    val createdAt: Long
) {
    fun toDomain(): SkipException = SkipException(
        id = id, date = date,
        reason = SkipReason.valueOf(reason),
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(skip: SkipException) = SkipExceptionEntity(
            id = skip.id, date = skip.date,
            reason = skip.reason.name,
            createdAt = skip.createdAt
        )
    }
}
