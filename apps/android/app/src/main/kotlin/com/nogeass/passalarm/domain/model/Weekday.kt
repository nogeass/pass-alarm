package com.nogeass.passalarm.domain.model

enum class Weekday(val bit: Int, val label: String, val calendarValue: Int) {
    MONDAY(1, "月", java.util.Calendar.MONDAY),
    TUESDAY(2, "火", java.util.Calendar.TUESDAY),
    WEDNESDAY(4, "水", java.util.Calendar.WEDNESDAY),
    THURSDAY(8, "木", java.util.Calendar.THURSDAY),
    FRIDAY(16, "金", java.util.Calendar.FRIDAY),
    SATURDAY(32, "土", java.util.Calendar.SATURDAY),
    SUNDAY(64, "日", java.util.Calendar.SUNDAY);

    companion object {
        fun fromMask(mask: Int): Set<Weekday> =
            entries.filter { mask and it.bit != 0 }.toSet()

        fun toMask(days: Set<Weekday>): Int =
            days.fold(0) { acc, day -> acc or day.bit }

        fun containsCalendarDay(mask: Int, calendarDay: Int): Boolean =
            entries.any { it.calendarValue == calendarDay && mask and it.bit != 0 }
    }
}
