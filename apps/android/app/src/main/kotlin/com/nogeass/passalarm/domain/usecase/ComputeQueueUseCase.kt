package com.nogeass.passalarm.domain.usecase

import com.nogeass.passalarm.domain.model.Occurrence
import com.nogeass.passalarm.domain.model.Weekday
import com.nogeass.passalarm.domain.repository.AlarmPlanRepository
import com.nogeass.passalarm.domain.repository.AppSettingsRepository
import com.nogeass.passalarm.domain.repository.HolidayRepository
import com.nogeass.passalarm.domain.repository.SkipExceptionRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ComputeQueueUseCase @Inject constructor(
    private val planRepository: AlarmPlanRepository,
    private val skipRepository: SkipExceptionRepository,
    private val holidayRepository: HolidayRepository,
    private val appSettingsRepository: AppSettingsRepository
) {
    companion object {
        const val LOOKAHEAD_DAYS = 90
    }

    suspend fun execute(now: Date = Date()): List<Occurrence> {
        val plans = planRepository.fetchEnabled()
        if (plans.isEmpty()) return emptyList()

        val appSettings = appSettingsRepository.get()

        val calendar = Calendar.getInstance()
        calendar.time = now
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time

        calendar.add(Calendar.DAY_OF_YEAR, LOOKAHEAD_DAYS)
        val endDate = calendar.time

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val fromStr = dateFormat.format(startDate)
        val toStr = dateFormat.format(endDate)

        val holidays = if (appSettings.holidayAutoSkip) {
            holidayRepository.fetchByDateRange(fromStr, toStr)
        } else emptyList()
        val holidayDates = holidays.map { it.date }.toSet()

        val allOccurrences = mutableListOf<Occurrence>()

        for (plan in plans) {
            val skips = skipRepository.fetchByPlanAndDateRange(plan.id, fromStr, toStr)
            val skipDates = skips.map { it.date }.toSet()

            val timeParts = plan.timeHHmm.split(":")
            if (timeParts.size != 2) continue
            val hour = timeParts[0].toIntOrNull() ?: continue
            val minute = timeParts[1].toIntOrNull() ?: continue

            calendar.time = startDate

            while (calendar.time.before(endDate)) {
                val dateStr = dateFormat.format(calendar.time)
                val calendarWeekday = calendar.get(Calendar.DAY_OF_WEEK)

                if (Weekday.containsCalendarDay(plan.weekdaysMask, calendarWeekday)) {
                    val fireCalendar = calendar.clone() as Calendar
                    fireCalendar.set(Calendar.HOUR_OF_DAY, hour)
                    fireCalendar.set(Calendar.MINUTE, minute)
                    fireCalendar.set(Calendar.SECOND, 0)
                    fireCalendar.set(Calendar.MILLISECOND, 0)

                    if (fireCalendar.time.after(now)) {
                        val isHolidaySkip = appSettings.holidayAutoSkip && holidayDates.contains(dateStr)
                        val isManualSkip = skipDates.contains(dateStr)
                        val isSkipped = isHolidaySkip || isManualSkip
                        val skipReason = when {
                            isHolidaySkip -> "祝日"
                            isManualSkip -> "手動スキップ"
                            else -> null
                        }

                        allOccurrences.add(
                            Occurrence(
                                planId = plan.id,
                                planLabel = plan.label,
                                date = dateStr,
                                timeHHmm = plan.timeHHmm,
                                fireAtEpoch = fireCalendar.timeInMillis,
                                isSkipped = isSkipped,
                                skipReason = skipReason
                            )
                        )
                    }
                }
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        return allOccurrences.sortedBy { it.fireAtEpoch }
    }
}
