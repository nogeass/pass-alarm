package com.nogeass.passalarm.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nogeass.passalarm.domain.model.SkipException
import com.nogeass.passalarm.domain.model.SkipReason

@Entity(
    tableName = "skip_exception",
    indices = [
        Index(value = ["date"]),
        Index(value = ["planId", "date"], name = "idx_skip_planId_date")
    ]
)
data class SkipExceptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long = 0,
    val date: String,
    val reason: String,
    val createdAt: Long
) {
    fun toDomain(): SkipException = SkipException(
        id = id, planId = planId, date = date,
        reason = SkipReason.valueOf(reason),
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(skip: SkipException) = SkipExceptionEntity(
            id = skip.id, planId = skip.planId, date = skip.date,
            reason = skip.reason.name,
            createdAt = skip.createdAt
        )
    }
}
