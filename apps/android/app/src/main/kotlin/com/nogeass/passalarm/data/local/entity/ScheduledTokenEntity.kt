package com.nogeass.passalarm.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nogeass.passalarm.domain.model.ScheduledToken
import com.nogeass.passalarm.domain.model.TokenStatus

@Entity(
    tableName = "scheduled_token",
    indices = [Index("status"), Index("fireAtEpoch"), Index("osIdentifier")]
)
data class ScheduledTokenEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val date: String,
    val fireAtEpoch: Long,
    val osIdentifier: Int,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): ScheduledToken = ScheduledToken(
        id = id, planId = planId, date = date,
        fireAtEpoch = fireAtEpoch, osIdentifier = osIdentifier,
        status = TokenStatus.valueOf(status),
        createdAt = createdAt, updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(token: ScheduledToken) = ScheduledTokenEntity(
            id = token.id, planId = token.planId, date = token.date,
            fireAtEpoch = token.fireAtEpoch, osIdentifier = token.osIdentifier,
            status = token.status.name,
            createdAt = token.createdAt, updatedAt = token.updatedAt
        )
    }
}
