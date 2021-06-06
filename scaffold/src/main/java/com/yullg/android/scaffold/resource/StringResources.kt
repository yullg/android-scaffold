package com.yullg.android.scaffold.resource

import com.yullg.android.scaffold.R
import com.yullg.android.scaffold.app.Scaffold

object StringResources {

    object Date {

        val am get() = Scaffold.context.getString(R.string.yg_string_resources_date_am)
        val pm get() = Scaffold.context.getString(R.string.yg_string_resources_date_pm)

        object Month {
            val january
                get() = Scaffold.context.getString(R.string.yg_string_resources_date_month_january)
            val february
                get() = Scaffold.context.getString(R.string.yg_string_resources_date_month_february)
            val march
                get() = Scaffold.context.getString(R.string.yg_string_resources_date_month_march)
            val april
                get() = Scaffold.context.getString(R.string.yg_string_resources_date_month_april)
            val may
                get() = Scaffold.context.getString(R.string.yg_string_resources_date_month_may)
            val june
                get() = Scaffold.context.getString(R.string.yg_string_resources_date_month_june)
            val july
                get() = Scaffold.context.getString(R.string.yg_string_resources_date_month_july)
            val august
                get() = Scaffold.context.getString(R.string.yg_string_resources_date_month_august)
            val september
                get() = Scaffold.context.getString(R.string.yg_string_resources_date_month_september)
            val october
                get() = Scaffold.context.getString(R.string.yg_string_resources_date_month_october)
            val november
                get() = Scaffold.context.getString(R.string.yg_string_resources_date_month_november)
            val december
                get() = Scaffold.context.getString(R.string.yg_string_resources_date_month_december)
        }

        object Week {
            val sunday
                get() = Scaffold.context.getString(R.string.yg_string_resources_date_week_sunday)
            val monday
                get() = Scaffold.context.getString(R.string.yg_string_resources_date_week_monday)
            val tuesday
                get() = Scaffold.context.getString(R.string.yg_string_resources_date_week_tuesday)
            val wednesday
                get() = Scaffold.context.getString(R.string.yg_string_resources_date_week_wednesday)
            val thursday
                get() = Scaffold.context.getString(R.string.yg_string_resources_date_week_thursday)
            val friday
                get() = Scaffold.context.getString(R.string.yg_string_resources_date_week_friday)
            val saturday
                get() = Scaffold.context.getString(R.string.yg_string_resources_date_week_saturday)
        }

    }

    object MeasureUnit {

        object Length {
            val nanometre
                get() = Scaffold.context.getString(R.string.yg_string_resources_measure_unit_length_nanometre)
            val micrometer
                get() = Scaffold.context.getString(R.string.yg_string_resources_measure_unit_length_micrometer)
            val millimeter
                get() = Scaffold.context.getString(R.string.yg_string_resources_measure_unit_length_millimeter)
            val centimeter
                get() = Scaffold.context.getString(R.string.yg_string_resources_measure_unit_length_centimeter)
            val meter
                get() = Scaffold.context.getString(R.string.yg_string_resources_measure_unit_length_meter)
            val kilometer
                get() = Scaffold.context.getString(R.string.yg_string_resources_measure_unit_length_kilometer)
        }

        object Time {
            val millisecond
                get() = Scaffold.context.getString(R.string.yg_string_resources_measure_unit_time_millisecond)
            val second
                get() = Scaffold.context.getString(R.string.yg_string_resources_measure_unit_time_second)
            val minute
                get() = Scaffold.context.getString(R.string.yg_string_resources_measure_unit_time_minute)
            val hour
                get() = Scaffold.context.getString(R.string.yg_string_resources_measure_unit_time_hour)
            val day
                get() = Scaffold.context.getString(R.string.yg_string_resources_measure_unit_time_day)
        }

        object Mass {
            val milligram
                get() = Scaffold.context.getString(R.string.yg_string_resources_measure_unit_mass_milligram)
            val gram
                get() = Scaffold.context.getString(R.string.yg_string_resources_measure_unit_mass_gram)
            val kilogram
                get() = Scaffold.context.getString(R.string.yg_string_resources_measure_unit_mass_kilogram)
            val tonne
                get() = Scaffold.context.getString(R.string.yg_string_resources_measure_unit_mass_tonne)
        }

        object Temperature {
            val centigrade
                get() = Scaffold.context.getString(R.string.yg_string_resources_measure_unit_temperature_centigrade)
            val fahrenheit
                get() = Scaffold.context.getString(R.string.yg_string_resources_measure_unit_temperature_fahrenheit)
        }

    }

}