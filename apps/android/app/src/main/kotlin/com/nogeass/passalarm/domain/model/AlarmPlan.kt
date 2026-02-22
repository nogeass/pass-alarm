package com.nogeass.passalarm.domain.model

data class AlarmPlan(
    val id: Long = 0,
    val isEnabled: Boolean = true,
    val timeHHmm: String = "07:00",
    val weekdaysMask: Int = 0b00011111, // Mon-Fri
    val repeatCount: Int = 10,
    val intervalMin: Int = 5,
    val holidayAutoSkip: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
