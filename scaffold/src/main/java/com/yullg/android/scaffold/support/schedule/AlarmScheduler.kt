package com.yullg.android.scaffold.support.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import com.yullg.android.scaffold.app.Scaffold

open class AlarmScheduler(private val operation: PendingIntent) {

    private val alarmManager: AlarmManager by lazy {
        ContextCompat.getSystemService(Scaffold.context, AlarmManager::class.java)!!
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= 31) {
            alarmManager.canScheduleExactAlarms()
        } else true
    }

    fun schedule(type: Int, triggerAtMillis: Long) {
        alarmManager.set(type, triggerAtMillis, operation)
    }

    fun scheduleAndAllowWhileIdle(type: Int, triggerAtMillis: Long) {
        AlarmManagerCompat.setAndAllowWhileIdle(alarmManager, type, triggerAtMillis, operation)
    }

    fun scheduleExact(type: Int, triggerAtMillis: Long) {
        alarmManager.setExact(type, triggerAtMillis, operation)
    }

    fun scheduleExactAndAllowWhileIdle(type: Int, triggerAtMillis: Long) {
        AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, type, triggerAtMillis, operation)
    }

    fun scheduleRepeating(type: Int, triggerAtMillis: Long, intervalMillis: Long) {
        alarmManager.setInexactRepeating(type, triggerAtMillis, intervalMillis, operation)
    }

    fun scheduleWindow(type: Int, windowStartMillis: Long, windowLengthMillis: Long) {
        alarmManager.setWindow(type, windowStartMillis, windowLengthMillis, operation)
    }

    fun cancel() = alarmManager.cancel(operation)

}

open class BroadcastAlarmScheduler(cls: Class<out BroadcastReceiver>) :
    AlarmScheduler(
        PendingIntent.getBroadcast(
            Scaffold.context,
            0,
            Intent(Scaffold.context, cls),
            if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0
        )
    )

open class ServiceAlarmScheduler(cls: Class<out Service>) :
    AlarmScheduler(
        PendingIntent.getService(
            Scaffold.context,
            0,
            Intent(Scaffold.context, cls),
            if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0
        )
    )

@RequiresApi(26)
open class ForegroundServiceAlarmScheduler(cls: Class<out Service>) :
    AlarmScheduler(
        PendingIntent.getForegroundService(
            Scaffold.context,
            0,
            Intent(Scaffold.context, cls),
            if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0
        )
    )