package com.yullg.android.scaffold.helper

import java.util.*

object DateHelper {

    /**
     * <p>Checks if two date objects represent the same instant in time.</p>
     *
     * <p>This method compares the long millisecond time of the two objects.</p>
     */
    fun isSameInstant(date1: Date, date2: Date): Boolean = date1.time == date2.time

    /**
     * <p>Checks if two calendar objects represent the same instant in time.</p>
     *
     * <p>This method compares the long millisecond time of the two objects.</p>
     */
    fun isSameInstant(cal1: Calendar, cal2: Calendar): Boolean = cal1.time.time == cal2.time.time

    /**
     * <p>Checks if two date objects are on the same day.</p>
     */
    fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return isSameDay(cal1, cal2)
    }

    /**
     * <p>Checks if two calendar objects are on the same day.</p>
     */
    fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)

    /**
     * <p>Checks if two date objects are on the same week.</p>
     */
    fun isSameWeek(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return isSameWeek(cal1, cal2)
    }

    /**
     * <p>Checks if two calendar objects are on the same week.</p>
     */
    fun isSameWeek(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)

    /**
     * <p>Checks if two date objects are on the same month.</p>
     */
    fun isSameMonth(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return isSameMonth(cal1, cal2)
    }

    /**
     * <p>Checks if two calendar objects are on the same month.</p>
     */
    fun isSameMonth(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)

    /**
     * <p>Checks if two date objects are on the same year.</p>
     */
    fun isSameYear(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return isSameYear(cal1, cal2)
    }

    /**
     * <p>Checks if two calendar objects are on the same year.</p>
     */
    fun isSameYear(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)

    /**
     * Adds a number of years to a date returning a new object.
     * The original {@code Date} is unchanged.
     */
    fun addYears(date: Date, amount: Int): Date = add(date, Calendar.YEAR, amount)

    /**
     * Adds a number of months to a date returning a new object.
     * The original {@code Date} is unchanged.
     */
    fun addMonths(date: Date, amount: Int): Date = add(date, Calendar.MONTH, amount)

    /**
     * Adds a number of weeks to a date returning a new object.
     * The original {@code Date} is unchanged.
     */
    fun addWeeks(date: Date, amount: Int): Date = add(date, Calendar.WEEK_OF_YEAR, amount)

    /**
     * Adds a number of days to a date returning a new object.
     * The original {@code Date} is unchanged.
     */
    fun addDays(date: Date, amount: Int): Date = add(date, Calendar.DAY_OF_MONTH, amount)

    /**
     * Adds a number of hours to a date returning a new object.
     * The original {@code Date} is unchanged.
     */
    fun addHours(date: Date, amount: Int): Date = add(date, Calendar.HOUR_OF_DAY, amount)

    /**
     * Adds a number of minutes to a date returning a new object.
     * The original {@code Date} is unchanged.
     */
    fun addMinutes(date: Date, amount: Int): Date = add(date, Calendar.MINUTE, amount)

    /**
     * Adds a number of seconds to a date returning a new object.
     * The original {@code Date} is unchanged.
     */
    fun addSeconds(date: Date, amount: Int): Date = add(date, Calendar.SECOND, amount)

    /**
     * Adds a number of milliseconds to a date returning a new object.
     * The original {@code Date} is unchanged.
     */
    fun addMilliseconds(date: Date, amount: Int): Date = add(date, Calendar.MILLISECOND, amount)

    /**
     * Adds to a date returning a new object.
     * The original {@code Date} is unchanged.
     */
    private fun add(date: Date, calendarField: Int, amount: Int): Date {
        val c = Calendar.getInstance()
        c.time = date
        c.add(calendarField, amount)
        return c.time
    }

}

// <<< [commons-lang : 3.12.0] org.apache.commons.lang3.time.DateUtils