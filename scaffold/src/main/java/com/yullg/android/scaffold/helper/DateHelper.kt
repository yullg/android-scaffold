package com.yullg.android.scaffold.helper

import java.util.*

object DateHelper {

    const val MILLIS_PER_SECOND: Long = 1000
    const val MILLIS_PER_MINUTE: Long = 60 * MILLIS_PER_SECOND
    const val MILLIS_PER_HOUR: Long = 60 * MILLIS_PER_MINUTE
    const val MILLIS_PER_DAY: Long = 24 * MILLIS_PER_HOUR

    fun isSameInstant(date1: Date, date2: Date): Boolean = date1.time == date2.time

    fun isSameInstant(cal1: Calendar, cal2: Calendar): Boolean = cal1.time.time == cal2.time.time

    fun isSameMillisecond(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return isSameMillisecond(cal1, cal2)
    }

    fun isSameMillisecond(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.MILLISECOND) == cal2.get(Calendar.MILLISECOND) &&
                cal1.get(Calendar.SECOND) == cal2.get(Calendar.SECOND) &&
                cal1.get(Calendar.MINUTE) == cal2.get(Calendar.MINUTE) &&
                cal1.get(Calendar.HOUR_OF_DAY) == cal2.get(Calendar.HOUR_OF_DAY) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)

    fun isSameSecond(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return isSameSecond(cal1, cal2)
    }

    fun isSameSecond(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.SECOND) == cal2.get(Calendar.SECOND) &&
                cal1.get(Calendar.MINUTE) == cal2.get(Calendar.MINUTE) &&
                cal1.get(Calendar.HOUR_OF_DAY) == cal2.get(Calendar.HOUR_OF_DAY) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)

    fun isSameMinute(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return isSameMinute(cal1, cal2)
    }

    fun isSameMinute(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.MINUTE) == cal2.get(Calendar.MINUTE) &&
                cal1.get(Calendar.HOUR_OF_DAY) == cal2.get(Calendar.HOUR_OF_DAY) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)

    fun isSameHour(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return isSameHour(cal1, cal2)
    }

    fun isSameHour(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.HOUR_OF_DAY) == cal2.get(Calendar.HOUR_OF_DAY) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)

    fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return isSameDay(cal1, cal2)
    }

    fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)

    fun isSameWeek(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return isSameWeek(cal1, cal2)
    }

    fun isSameWeek(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)

    fun isSameMonth(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return isSameMonth(cal1, cal2)
    }

    fun isSameMonth(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)

    fun isSameYear(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return isSameYear(cal1, cal2)
    }

    fun isSameYear(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)

    fun clearTime(date: Date, timeZone: TimeZone? = null): Date {
        return Calendar.getInstance(timeZone ?: TimeZone.getDefault()).apply {
            time = date
            doClearTime(this)
        }.time
    }

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

}