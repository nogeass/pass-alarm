package com.nogeass.passalarm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nogeass.passalarm.domain.model.Holiday

@Entity(tableName = "holiday_jp")
data class HolidayJpEntity(
    @PrimaryKey val date: String,
    val nameJa: String
) {
    fun toDomain(): Holiday = Holiday(date = date, nameJa = nameJa)

    companion object {
        fun fromDomain(holiday: Holiday) = HolidayJpEntity(
            date = holiday.date, nameJa = holiday.nameJa
        )
    }
}
