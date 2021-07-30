package com.yullg.android.scaffold.support.schedule

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.SystemClock
import androidx.annotation.CallSuper
import androidx.annotation.IntRange
import com.yullg.android.scaffold.app.Scaffold
import com.yullg.android.scaffold.internal.ScaffoldLogger
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min

private const val SCHEDULER_HANDLER: Int = 1
private const val SCHEDULER_ALARM: Int = 2

open class TenaciousScheduler(
    private val uniqueName: String,
    @IntRange(from = 1) private val handlerScheduleInterval: Long = 5_000,
    @IntRange(from = 60000) private val alarmScheduleInterval: Long = 900_000,
    @IntRange(from = 1) private val maxHandlerScheduleIdleDuration: Long = 120_000,
    private val switcherIntervalSupplier: SwitcherIntervalSupplier =
        LinearSwitcherIntervalSupplier(180_000, 3600_000),
    private val alarmToHandler: Boolean = false,
    private val runnable: Runnable
) : AutoCloseable {

    private val screenBroadcastReceiver: ScreenBroadcastReceiver
    private val handlerSchedulerRunnable: HandlerSchedulerRunnable
    private val alarmSchedulerRunnable: AlarmSchedulerRunnable
    private val switcherSchedulerRunnable: SwitcherSchedulerRunnable
    private val handlerScheduler: MainHandlerScheduler
    private val alarmScheduler: DirectAlarmScheduler
    private val switcherScheduler: DirectAlarmScheduler

    init {
        screenBroadcastReceiver = ScreenBroadcastReceiver()
        handlerSchedulerRunnable = HandlerSchedulerRunnable()
        alarmSchedulerRunnable = AlarmSchedulerRunnable()
        switcherSchedulerRunnable = SwitcherSchedulerRunnable()
        handlerScheduler = MainHandlerScheduler(handlerSchedulerRunnable)
        alarmScheduler = DirectAlarmScheduler(uniqueName, alarmSchedulerRunnable)
        switcherScheduler = DirectAlarmScheduler(
            uniqueName + "_switcher",
            switcherSchedulerRunnable
        )
    }

    private var currScheduler: Int = SCHEDULER_HANDLER

    fun schedule() {
        currScheduler = SCHEDULER_HANDLER
        handlerSchedulerRunnable.reset()
        handlerScheduler.schedule()
        startSwitcherScheduler(true)
        Scaffold.context.registerReceiver(screenBroadcastReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        })
    }

    fun cancel() {
        Scaffold.context.unregisterReceiver(screenBroadcastReceiver)
        handlerScheduler.cancel()
        alarmScheduler.cancel()
        switcherScheduler.cancel()
    }

    @CallSuper
    override fun close() {
        alarmScheduler.close()
        switcherScheduler.close()
        cancel()
    }

    private fun startSwitcherScheduler(resetIntervalSupplier: Boolean) {
        if (resetIntervalSupplier) {
            switcherIntervalSupplier.reset()
        }
        val switcherInterval = switcherIntervalSupplier.get()
        if (ScaffoldLogger.isDebugEnabled()) {
            ScaffoldLogger.debug("[TenaciousScheduler] SS will run after ${switcherInterval / 1000} seconds")
        }
        switcherScheduler.scheduleAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + switcherInterval
        )
    }

    private inner class ScreenBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null || intent == null) return
            if (Intent.ACTION_SCREEN_ON == intent.action) {
                switcherScheduler.cancel()
            } else if (Intent.ACTION_SCREEN_OFF == intent.action) {
                startSwitcherScheduler(true)
            }
        }

    }

    private inner class HandlerSchedulerRunnable : Runnable {

        var lastScheduleTime: Long = -1
        var maxScheduleInterval: Long = Long.MIN_VALUE

        override fun run() {
            try {
                val nowTime = SystemClock.elapsedRealtime()
                if (lastScheduleTime > 0) {
                    val scheduleInterval = nowTime - lastScheduleTime
                    if (scheduleInterval > maxScheduleInterval) {
                        if (ScaffoldLogger.isDebugEnabled()) {
                            ScaffoldLogger.debug("[TenaciousScheduler] HSR will update 'maxScheduleInterval' : scheduleInterval = $scheduleInterval, maxScheduleInterval = $maxScheduleInterval")
                        }
                        maxScheduleInterval = scheduleInterval
                    } else {
                        if (ScaffoldLogger.isDebugEnabled()) {
                            ScaffoldLogger.debug("[TenaciousScheduler] HSR did not update 'maxScheduleInterval' : scheduleInterval = $scheduleInterval, maxScheduleInterval = $maxScheduleInterval")
                        }
                    }
                } else {
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[TenaciousScheduler] HSR first run")
                    }
                }
                (SCHEDULER_HANDLER == currScheduler).let {
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[TenaciousScheduler] HSR run : $it")
                    }
                    if (it) {
                        runnable.run()
                    }
                }
            } finally {
                lastScheduleTime = SystemClock.elapsedRealtime()
                handlerScheduler.scheduleDelayed(handlerScheduleInterval)
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[TenaciousScheduler] HSR finished running at $lastScheduleTime and will rerun after ${handlerScheduleInterval / 1000} seconds")
                }
            }
        }

        fun reset() {
            lastScheduleTime = -1
            maxScheduleInterval = Long.MIN_VALUE
            if (ScaffoldLogger.isDebugEnabled()) {
                ScaffoldLogger.debug("[TenaciousScheduler] HandlerSchedulerRunnable has been reset")
            }
        }

    }

    private inner class AlarmSchedulerRunnable : Runnable {

        override fun run() {
            if (SCHEDULER_ALARM == currScheduler) {
                runnable.run()
            }
        }

    }

    private inner class SwitcherSchedulerRunnable : Runnable {

        override fun run() {
            val maxHandlerScheduleInterval = handlerSchedulerRunnable.maxScheduleInterval
            if (alarmToHandler) {
                try {
                    if (maxHandlerScheduleInterval > maxHandlerScheduleIdleDuration) {
                        currScheduler = SCHEDULER_ALARM
                        alarmScheduler.scheduleRepeating(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime(),
                            alarmScheduleInterval
                        )
                    } else {
                        alarmScheduler.cancel()
                        currScheduler = SCHEDULER_HANDLER
                    }
                } finally {
                    handlerSchedulerRunnable.reset()
                    startSwitcherScheduler(false)
                }
            } else {
                if (maxHandlerScheduleInterval > maxHandlerScheduleIdleDuration) {
                    currScheduler = SCHEDULER_ALARM
                    alarmScheduler.scheduleRepeating(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime(),
                        alarmScheduleInterval
                    )
                    handlerScheduler.cancel()
                    switcherScheduler.cancel()
                } else {
                    handlerSchedulerRunnable.reset()
                    startSwitcherScheduler(false)
                }
            }
        }

    }

}

interface SwitcherIntervalSupplier {

    fun get(): Long

    fun reset() {}

}

class FixedSwitcherIntervalSupplier(
    @IntRange(from = 60000) private val value: Long
) : SwitcherIntervalSupplier {

    override fun get(): Long = value

}

class LinearSwitcherIntervalSupplier(
    @IntRange(from = 60000) private val value: Long,
    @IntRange(from = 60000) private val maxValue: Long,
) : SwitcherIntervalSupplier {

    private val currValue = AtomicLong(0)

    override fun get(): Long = if (currValue.get() < maxValue) {
        min(currValue.addAndGet(value), maxValue)
    } else maxValue

    override fun reset() {
        currValue.set(0)
    }

}