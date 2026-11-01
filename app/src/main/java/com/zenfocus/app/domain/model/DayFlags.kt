package com.zenfocus.app.domain.model

import java.util.Calendar

/**
 * 7-bit bitmask representation of days of the week:
 * MONDAY = 1, TUESDAY = 2, WEDNESDAY = 4, THURSDAY = 8,
 * FRIDAY = 16, SATURDAY = 32, SUNDAY = 64
 */
enum class DayFlags(val flag: Int, val calendarDay: Int) {
    MONDAY(1, Calendar.MONDAY),
    TUESDAY(2, Calendar.TUESDAY),
    WEDNESDAY(4, Calendar.WEDNESDAY),
    THURSDAY(8, Calendar.THURSDAY),
    FRIDAY(16, Calendar.FRIDAY),
    SATURDAY(32, Calendar.SATURDAY),
    SUNDAY(64, Calendar.SUNDAY);

    companion object {
        const val ALL_DAYS = 1 or 2 or 4 or 8 or 16 or 32 or 64 // 127
        const val WEEKDAYS = 1 or 2 or 4 or 8 or 16             // 31
        const val WEEKENDS = 32 or 64                          // 96

        fun fromCalendarDay(calendarDay: Int): DayFlags? {
            return entries.firstOrNull { it.calendarDay == calendarDay }
        }

        fun hasFlag(bitmask: Int, day: DayFlags): Boolean {
            return (bitmask and day.flag) != 0
        }

        fun isCurrentlyActive(daysBitmask: Int, startMinutes: Int, endMinutes: Int, now: Calendar = Calendar.getInstance()): Boolean {
            val currentMinute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            return if (startMinutes <= endMinutes) {
                // Daytime interval (e.g. 09:00 - 17:00)
                val dayFlag = fromCalendarDay(now.get(Calendar.DAY_OF_WEEK)) ?: return false
                hasFlag(daysBitmask, dayFlag) && (currentMinute in startMinutes..endMinutes)
            } else {
                // Overnight interval spanning midnight (e.g. 22:00 - 06:00)
                if (currentMinute >= startMinutes) {
                    // Evening portion: belongs to today's schedule
                    val today = fromCalendarDay(now.get(Calendar.DAY_OF_WEEK)) ?: return false
                    hasFlag(daysBitmask, today)
                } else if (currentMinute <= endMinutes) {
                    // Morning portion: belongs to yesterday's night schedule
                    val prevCal = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
                    val yesterday = fromCalendarDay(prevCal.get(Calendar.DAY_OF_WEEK)) ?: return false
                    hasFlag(daysBitmask, yesterday)
                } else {
                    false
                }
            }
        }
    }
}
