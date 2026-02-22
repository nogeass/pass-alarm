package com.nogeass.passalarm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nogeass.passalarm.domain.model.AlarmPlan

@Entity(tableName = "alarm_plan")
data class AlarmPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val isEnabled: Boolean,
    val timeHHmm: String,
    val weekdaysMask: Int,
    val repeatCount: Int,
    val intervalMin: Int,
    val holidayAutoSkip: Boolean,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): AlarmPlan = AlarmPlan(
        id = id, isEnabled = isEnabled, timeHHmm = timeHHmm,
        weekdaysMask = weekdaysMask, repeatCount = repeatCount,
        intervalMin = intervalMin, holidayAutoSkip = holidayAutoSkip,
        createdAt = createdAt, updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(plan: AlarmPlan) = AlarmPlanEntity(
            id = plan.id, isEnabled = plan.isEnabled, timeHHmm = plan.timeHHmm,
            weekdaysMask = plan.weekdaysMask, repeatCount = plan.repeatCount,
            intervalMin = plan.intervalMin, holidayAutoSkip = plan.holidayAutoSkip,
            createdAt = plan.createdAt, updatedAt = plan.updatedAt
        )
    }
}
