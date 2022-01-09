package com.yullg.android.scaffold.helper

import java.util.*

/**
 * 提供日期相关的辅助功能
 */
object DateHelper {

    const val MILLIS_PER_SECOND: Long = 1000
    const val MILLIS_PER_MINUTE: Long = 60 * MILLIS_PER_SECOND
    const val MILLIS_PER_HOUR: Long = 60 * MILLIS_PER_MINUTE
    const val MILLIS_PER_DAY: Long = 24 * MILLIS_PER_HOUR

    fun yesterday(date: Date): Date = yesterday(dateToCalendar(date)).time

    fun yesterday(calendar: Calendar): Calendar {
        return (calendar.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
    }

    fun tomorrow(date: Date): Date = tomorrow(dateToCalendar(date)).time

    fun tomorrow(calendar: Calendar): Calendar {
        return (calendar.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    fun isSameInstant(date1: Date, date2: Date): Boolean = date1.time == date2.time

    fun isSameInstant(cal1: Calendar, cal2: Calendar): Boolean = cal1.time.time == cal2.time.time

    fun isSameMillisecond(date1: Date, date2: Date): Boolean =
        isSameMillisecond(dateToCalendar(date1), dateToCalendar(date2))

    fun isSameMillisecond(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.MILLISECOND) == cal2.get(Calendar.MILLISECOND) &&
                cal1.get(Calendar.SECOND) == cal2.get(Calendar.SECOND) &&
                cal1.get(Calendar.MINUTE) == cal2.get(Calendar.MINUTE) &&
                cal1.get(Calendar.HOUR_OF_DAY) == cal2.get(Calendar.HOUR_OF_DAY) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)

    fun isSameSecond(date1: Date, date2: Date): Boolean =
        isSameSecond(dateToCalendar(date1), dateToCalendar(date2))

    fun isSameSecond(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.SECOND) == cal2.get(Calendar.SECOND) &&
                cal1.get(Calendar.MINUTE) == cal2.get(Calendar.MINUTE) &&
                cal1.get(Calendar.HOUR_OF_DAY) == cal2.get(Calendar.HOUR_OF_DAY) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)

    fun isSameMinute(date1: Date, date2: Date): Boolean =
        isSameMinute(dateToCalendar(date1), dateToCalendar(date2))

    fun isSameMinute(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.MINUTE) == cal2.get(Calendar.MINUTE) &&
                cal1.get(Calendar.HOUR_OF_DAY) == cal2.get(Calendar.HOUR_OF_DAY) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)

    fun isSameHour(date1: Date, date2: Date): Boolean =
        isSameHour(dateToCalendar(date1), dateToCalendar(date2))

    fun isSameHour(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.HOUR_OF_DAY) == cal2.get(Calendar.HOUR_OF_DAY) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)

    fun isSameDay(date1: Date, date2: Date): Boolean =
        isSameDay(dateToCalendar(date1), dateToCalendar(date2))

    fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)

    fun isSameWeek(date1: Date, date2: Date): Boolean =
        isSameWeek(dateToCalendar(date1), dateToCalendar(date2))

    fun isSameWeek(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)

    fun isSameMonth(date1: Date, date2: Date): Boolean =
        isSameMonth(dateToCalendar(date1), dateToCalendar(date2))

    fun isSameMonth(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)

    fun isSameYear(date1: Date, date2: Date): Boolean =
        isSameYear(dateToCalendar(date1), dateToCalendar(date2))

    fun isSameYear(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)

    fun getFirstDayOfWeek(date: Date): Date =
        getFirstDayOfWeek(dateToCalendar(date)).time

    fun getFirstDayOfWeek(calendar: Calendar): Calendar {
        return (calendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        }
    }

    fun getLastDayOfWeek(date: Date): Date =
        getLastDayOfWeek(dateToCalendar(date)).time

    fun getLastDayOfWeek(calendar: Calendar): Calendar {
        return getFirstDayOfWeek(calendar).apply {
            add(Calendar.DAY_OF_YEAR, 6)
        }
    }

    fun getFirstDayOfMonth(date: Date): Date =
        getFirstDayOfMonth(dateToCalendar(date)).time

    fun getFirstDayOfMonth(calendar: Calendar): Calendar {
        return (calendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    fun getLastDayOfMonth(date: Date): Date =
        getLastDayOfMonth(dateToCalendar(date)).time

    fun getLastDayOfMonth(calendar: Calendar): Calendar {
        return (calendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }
    }

    fun clearTime(date: Date): Date =
        clearTime(dateToCalendar(date)).time

    fun clearTime(calendar: Calendar): Calendar {
        return (calendar.clone() as Calendar).apply {
            doClearTime(this)
        }
    }

    private fun doClearTime(calendar: Calendar) {
        var timeInMillis = calendar.timeInMillis
        timeInMillis -= calendar.get(Calendar.MILLISECOND)
        timeInMillis -= (calendar.get(Calendar.SECOND) * MILLIS_PER_SECOND)
        timeInMillis -= (calendar.get(Calendar.MINUTE) * MILLIS_PER_MINUTE)
        timeInMillis -= (calendar.get(Calendar.HOUR_OF_DAY) * MILLIS_PER_HOUR)
        calendar.timeInMillis = timeInMillis
    }

    fun dateToCalendar(date: Date): Calendar = Calendar.getInstance().apply {
        time = date
    }

    fun dateToCalendar(date: Date, zone: TimeZone): Calendar = Calendar.getInstance(zone).apply {
        time = date
    }

    fun dateToCalendar(date: Date, locale: Locale): Calendar = Calendar.getInstance(locale).apply {
        time = date
    }

    fun dateToCalendar(date: Date, zone: TimeZone, locale: Locale): Calendar =
        Calendar.getInstance(zone, locale).apply {
            time = date
        }

}