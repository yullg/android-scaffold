package com.yullg.android.scaffold.support.schedule

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.SystemClock
import androidx.annotation.CallSuper
import androidx.annotation.IntRange
import androidx.annotation.MainThread
import com.yullg.android.scaffold.app.Scaffold
import com.yullg.android.scaffold.internal.ScaffoldLogger
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max
import kotlin.math.min

private enum class ScheduleMode { NONE, HANDLER, ALARM, ALARM_AND_HANDLER }

@MainThread
open class TenaciousScheduler(
    private val uniqueName: String,
    @IntRange(from = 1) private val handlerScheduleInterval: Long = 5_000,
    @IntRange(from = 60000) private val alarmScheduleInterval: Long = 900_000,
    @IntRange(from = 1) private val maxHandlerScheduleIdleDuration: Long = 300_000,
    private val switcherIntervalSupplier: SwitcherIntervalSupplier =
        LinearSwitcherIntervalSupplier(600_000, 3600_000),
    private val alarmToHandler: Boolean = false,
    private val runnable: Runnable
) : AutoCloseable {

    private val handlerSchedulerRunnable: HandlerSchedulerRunnable
    private val alarmSchedulerRunnable: AlarmSchedulerRunnable
    private val switcherSchedulerRunnable: SwitcherSchedulerRunnable
    private val screenBroadcastReceiver: ScreenBroadcastReceiver
    private val handlerScheduler: MainHandlerScheduler
    private val alarmScheduler: DirectAlarmScheduler
    private val switcherScheduler: DirectAlarmScheduler

    private var scheduleMode: ScheduleMode = ScheduleMode.NONE

    init {
        handlerSchedulerRunnable = HandlerSchedulerRunnable()
        alarmSchedulerRunnable = AlarmSchedulerRunnable()
        switcherSchedulerRunnable = SwitcherSchedulerRunnable()
        screenBroadcastReceiver = ScreenBroadcastReceiver()
        handlerScheduler = MainHandlerScheduler(handlerSchedulerRunnable)
        alarmScheduler = DirectAlarmScheduler(uniqueName, alarmSchedulerRunnable)
        switcherScheduler = DirectAlarmScheduler(
            uniqueName + "_switcher",
            switcherSchedulerRunnable
        )
    }

    private fun switchScheduleMode(scheduleMode: ScheduleMode) {
        this.scheduleMode = scheduleMode
        when (scheduleMode) {
            ScheduleMode.HANDLER -> {
                alarmScheduler.cancel()
                handlerScheduler.cancel()
                handlerSchedulerRunnable.reset()
                handlerScheduler.scheduleDelayed(handlerScheduleInterval)
            }
            ScheduleMode.ALARM -> {
                handlerScheduler.cancel()
                handlerSchedulerRunnable.reset()
                alarmScheduler.scheduleRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + alarmScheduleInterval,
                    alarmScheduleInterval
                )
            }
            ScheduleMode.ALARM_AND_HANDLER -> {
                alarmScheduler.scheduleRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + alarmScheduleInterval,
                    alarmScheduleInterval
                )
                handlerScheduler.cancel()
                handlerSchedulerRunnable.reset()
                handlerScheduler.scheduleDelayed(handlerScheduleInterval)
            }
            ScheduleMode.NONE -> {
                alarmScheduler.cancel()
                handlerScheduler.cancel()
                handlerSchedulerRunnable.reset()
            }
        }
    }

    fun schedule() {
        if (ScheduleMode.NONE == scheduleMode) {
            switchScheduleMode(ScheduleMode.HANDLER)
            val switcherInterval = startSwitchScheduler(true)
            startScreenScheduler()
            if (ScaffoldLogger.isDebugEnabled()) {
                ScaffoldLogger.debug("[TenaciousScheduler] Scheduler started and SS will run after ${switcherInterval / 1000} seconds")
            }
        } else {
            if (ScaffoldLogger.isWarnEnabled()) {
                ScaffoldLogger.warn("[TenaciousScheduler] Schedule operation is ignored because the current schedule is not stopped")
            }
        }
    }

    fun cancel() {
        if (ScheduleMode.NONE != scheduleMode) {
            switchScheduleMode(ScheduleMode.NONE)
            stopSwitchScheduler()
            stopScreenScheduler()
            if (ScaffoldLogger.isDebugEnabled()) {
                ScaffoldLogger.debug("[TenaciousScheduler] Scheduler canceled")
            }
        } else {
            if (ScaffoldLogger.isWarnEnabled()) {
                ScaffoldLogger.warn("[TenaciousScheduler] Cancel operation is ignored because the current schedule is not started")
            }
        }
    }

    @CallSuper
    override fun close() {
        cancel()
        alarmScheduler.close()
        switcherScheduler.close()
        if (ScaffoldLogger.isDebugEnabled()) {
            ScaffoldLogger.debug("[TenaciousScheduler] Scheduler closed")
        }
    }

    private fun startSwitchScheduler(resetSupplier: Boolean): Long {
        if (resetSupplier) {
            switcherIntervalSupplier.reset()
        }
        val switcherInterval = switcherIntervalSupplier.get()
        switcherScheduler.scheduleAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + switcherInterval
        )
        return switcherInterval
    }

    private fun stopSwitchScheduler() {
        switcherScheduler.cancel()
    }

    private fun startScreenScheduler() {
        Scaffold.context.registerReceiver(screenBroadcastReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        })
    }

    private fun stopScreenScheduler() {
        Scaffold.context.unregisterReceiver(screenBroadcastReceiver)
    }

    private inner class HandlerSchedulerRunnable : Runnable {

        var lastScheduleTime: Long? = null
        var maxScheduleInterval: Long? = null

        override fun run() {
            if (ScheduleMode.NONE == scheduleMode) {
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[TenaciousScheduler] HSR deliver was interrupted because the schedule was not started")
                }
                return
            }
            try {
                val logMessageBuilder = StringBuilder()
                val nowTime = SystemClock.elapsedRealtime()
                if (ScaffoldLogger.isDebugEnabled()) {
                    lastScheduleTime.let { lst ->
                        if (lst == null) {
                            logMessageBuilder.append("[TenaciousScheduler] HSR start deliver : first run")
                        } else {
                            logMessageBuilder.append("[TenaciousScheduler] HSR start deliver : scheduleInterval = ${nowTime - lst}, maxScheduleInterval = $maxScheduleInterval")
                        }
                    }
                }
                lastScheduleTime?.let { lst ->
                    val scheduleInterval = nowTime - lst
                    maxScheduleInterval = maxScheduleInterval?.let { msi ->
                        max(scheduleInterval, msi)
                    } ?: scheduleInterval
                }
                (ScheduleMode.HANDLER == scheduleMode).let {
                    if (ScaffoldLogger.isDebugEnabled()) {
                        logMessageBuilder.append(", $it")
                        ScaffoldLogger.debug(logMessageBuilder.toString())
                    }
                    if (it) {
                        runnable.run()
                    }
                }
            } finally {
                lastScheduleTime = SystemClock.elapsedRealtime()
                handlerScheduler.scheduleDelayed(handlerScheduleInterval)
            }
        }

        fun reset() {
            lastScheduleTime = null
            maxScheduleInterval = null
            if (ScaffoldLogger.isDebugEnabled()) {
                ScaffoldLogger.debug("[TenaciousScheduler] HSR has been reset")
            }
        }

    }

    private inner class AlarmSchedulerRunnable : Runnable {

        override fun run() {
            if (ScheduleMode.NONE == scheduleMode) {
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[TenaciousScheduler] ASR deliver was interrupted because the schedule was not started")
                }
                return
            }
            (ScheduleMode.ALARM == scheduleMode || ScheduleMode.ALARM_AND_HANDLER == scheduleMode).let {
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[TenaciousScheduler] ASR start deliver : $it")
                }
                if (it) {
                    runnable.run()
                }
            }
        }

    }

    private inner class SwitcherSchedulerRunnable : Runnable {

        override fun run() {
            if (ScheduleMode.NONE == scheduleMode) {
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[TenaciousScheduler] SSR execution was interrupted because the schedule was not started")
                }
                return
            }
            val nowTime = SystemClock.elapsedRealtime()
            val handlerLastScheduleTime = handlerSchedulerRunnable.lastScheduleTime
            val handlerMaxScheduleInterval = handlerSchedulerRunnable.maxScheduleInterval
            if (alarmToHandler) {
                if (shouldSwitchToAlarmScheduler(
                        nowTime,
                        handlerLastScheduleTime,
                        handlerMaxScheduleInterval
                    )
                ) {
                    if (ScheduleMode.ALARM_AND_HANDLER != scheduleMode) {
                        switchScheduleMode(ScheduleMode.ALARM_AND_HANDLER)
                    } else {
                        handlerSchedulerRunnable.reset()
                    }
                    val switcherInterval = startSwitchScheduler(false)
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[TenaciousScheduler] SSR has performed switchover (nowTime = $nowTime, lastTime = $handlerLastScheduleTime, maxInterval = $handlerMaxScheduleInterval) and will rerun after ${switcherInterval / 1000} seconds : HS = ON, AS = ON, SS = ON")
                    }
                } else {
                    if (ScheduleMode.HANDLER != scheduleMode) {
                        switchScheduleMode(ScheduleMode.HANDLER)
                    } else {
                        handlerSchedulerRunnable.reset()
                    }
                    val switcherInterval = startSwitchScheduler(false)
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[TenaciousScheduler] SSR has performed switchover (nowTime = $nowTime, lastTime = $handlerLastScheduleTime, maxInterval = $handlerMaxScheduleInterval) and will rerun after ${switcherInterval / 1000} seconds : HS = ON, AS = OFF, SS = ON")
                    }
                }
            } else {
                if (shouldSwitchToAlarmScheduler(
                        nowTime,
                        handlerLastScheduleTime,
                        handlerMaxScheduleInterval
                    )
                ) {
                    if (ScheduleMode.ALARM != scheduleMode) {
                        switchScheduleMode(ScheduleMode.ALARM)
                    }
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[TenaciousScheduler] SSR has performed switchover (nowTime = $nowTime, lastTime = $handlerLastScheduleTime, maxInterval = $handlerMaxScheduleInterval) : HS = OFF, AS = ON, SS = OFF")
                    }
                } else {
                    handlerSchedulerRunnable.reset()
                    val switcherInterval = startSwitchScheduler(false)
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[TenaciousScheduler] SSR did not perform switchover (nowTime = $nowTime, lastTime = $handlerLastScheduleTime, maxInterval = $handlerMaxScheduleInterval) and will rerun after ${switcherInterval / 1000} seconds : HS = ON, AS = OFF, SS = ON")
                    }
                }
            }
        }

        private fun shouldSwitchToAlarmScheduler(
            nowTime: Long,
            handlerLastScheduleTime: Long?,
            handlerMaxScheduleInterval: Long?
        ): Boolean {
            return if (handlerLastScheduleTime == null) {
                true
            } else if (handlerMaxScheduleInterval == null) {
                nowTime - handlerLastScheduleTime > maxHandlerScheduleIdleDuration
            } else {
                handlerMaxScheduleInterval > maxHandlerScheduleIdleDuration
            }
        }

    }

    private inner class ScreenBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null || intent == null) return
            if (ScheduleMode.NONE == scheduleMode) {
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[TenaciousScheduler] SBR execution was interrupted because the schedule was not started")
                }
                return
            }
            if (ScheduleMode.HANDLER != scheduleMode) {
                switchScheduleMode(ScheduleMode.HANDLER)
            }
            if (Intent.ACTION_SCREEN_ON == intent.action) {
                stopSwitchScheduler()
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[TenaciousScheduler] SBR received ON and has performed switchover : HS = ON, AS = OFF, SS = OFF")
                }
            } else if (Intent.ACTION_SCREEN_OFF == intent.action) {
                val switcherInterval = startSwitchScheduler(true)
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[TenaciousScheduler] SBR received OFF and has started SS, SS will run after ${switcherInterval / 1000} seconds : HS = ON, AS = OFF, SS = ON")
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