package com.yullg.android.scaffold.support.schedule

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.CallSuper
import com.yullg.android.scaffold.app.Scaffold

open class DirectAlarmScheduler(private val uniqueName: String, private val listener: () -> Unit) :
    AutoCloseable {

    private val intentAction = "${Scaffold.context.packageName}.action.yg.$uniqueName"
    private val broadcastReceiver: BroadcastReceiver
    private val alarmScheduler: AlarmScheduler

    init {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                listener()
            }
        }
        Scaffold.context.registerReceiver(broadcastReceiver, IntentFilter(intentAction))
        alarmScheduler = AlarmScheduler(
            PendingIntent.getBroadcast(
                Scaffold.context,
                0,
                Intent(intentAction),
                if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0
            )
        )
    }

    fun canScheduleExactAlarms() = alarmScheduler.canScheduleExactAlarms()

    fun schedule(type: Int, triggerAtMillis: Long) =
        alarmScheduler.schedule(type, triggerAtMillis)

    fun scheduleAndAllowWhileIdle(type: Int, triggerAtMillis: Long) =
        alarmScheduler.scheduleAndAllowWhileIdle(type, triggerAtMillis)

    fun scheduleExact(type: Int, triggerAtMillis: Long) =
        alarmScheduler.scheduleExact(type, triggerAtMillis)

    fun scheduleExactAndAllowWhileIdle(type: Int, triggerAtMillis: Long) =
        alarmScheduler.scheduleExactAndAllowWhileIdle(type, triggerAtMillis)

    fun scheduleRepeating(type: Int, triggerAtMillis: Long, intervalMillis: Long) =
        alarmScheduler.scheduleRepeating(type, triggerAtMillis, intervalMillis)

    fun scheduleWindow(type: Int, windowStartMillis: Long, windowLengthMillis: Long) =
        alarmScheduler.scheduleWindow(type, windowStartMillis, windowLengthMillis)

    fun cancel() = alarmScheduler.cancel()

    @CallSuper
    override fun close() {
        try {
            cancel()
        } finally {
            Scaffold.context.unregisterReceiver(broadcastReceiver)
        }
    }

}