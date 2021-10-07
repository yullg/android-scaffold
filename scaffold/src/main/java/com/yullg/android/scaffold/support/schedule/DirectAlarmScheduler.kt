package com.yullg.android.scaffold.support.schedule

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.CallSuper
import com.yullg.android.scaffold.app.Scaffold

/**
 * 提供基于[AlarmManager]的任务调度功能。在实例构造时将要执行的任务[Runnable]固定下来，实例仅控制何时执行任务。
 *
 * 在实例创建过程中注册广播接收器，接收器直接调用[runnable]，相关调度方法将通过[AlarmManager]触发接收器执行。
 *
 * 调用[close()]将取消已安排的所有任务和注册的广播接收器并且实例销毁不可重用。
 */
open class DirectAlarmScheduler(private val uniqueName: String, private val runnable: Runnable) :
    AutoCloseable {

    private val intentAction = "${Scaffold.context.packageName}.action.yg.$uniqueName"
    private val broadcastReceiver: BroadcastReceiver
    private val alarmScheduler: AlarmScheduler

    init {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                runnable.run()
            }
        }
        Scaffold.context.registerReceiver(broadcastReceiver, IntentFilter(intentAction))
        alarmScheduler = BroadcastAlarmScheduler(Intent(intentAction))
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