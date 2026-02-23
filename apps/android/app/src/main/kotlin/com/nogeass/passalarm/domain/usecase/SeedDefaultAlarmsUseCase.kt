package com.nogeass.passalarm.domain.usecase

import com.nogeass.passalarm.domain.model.AlarmPlan
import com.nogeass.passalarm.domain.model.Weekday
import com.nogeass.passalarm.domain.repository.AlarmPlanRepository
import javax.inject.Inject

class SeedDefaultAlarmsUseCase @Inject constructor(
    private val planRepository: AlarmPlanRepository,
    private val reschedule: RescheduleNextNUseCase
) {
    suspend fun execute() {
        val existing = planRepository.fetchAll()
        if (existing.isNotEmpty()) return

        val weekdayMask = Weekday.toMask(
            setOf(Weekday.MONDAY, Weekday.TUESDAY, Weekday.WEDNESDAY, Weekday.THURSDAY, Weekday.FRIDAY)
        )

        val times = listOf("06:00", "07:00", "08:00", "09:00", "10:00")

        for (time in times) {
            planRepository.save(
                AlarmPlan(
                    isEnabled = false,
                    label = "",
                    timeHHmm = time,
                    weekdaysMask = weekdayMask,
                    repeatCount = 10,
                    intervalMin = 5
                )
            )
        }
    }
}
