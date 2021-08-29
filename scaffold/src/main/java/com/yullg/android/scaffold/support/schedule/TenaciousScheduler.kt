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
import com.yullg.android.scaffold.core.ExponentialIncreaseNumberSupplier
import com.yullg.android.scaffold.core.NumberSupplier
import com.yullg.android.scaffold.internal.ScaffoldLogger
import kotlin.math.max

private enum class ScheduleMode { NONE, HANDLER, ALARM, ALARM_AND_HANDLER }

@MainThread
open class TenaciousScheduler(
    private val uniqueName: String,
    @IntRange(from = 1) private val scheduleInterval: Long = 5_000,
    @IntRange(from = 60000) private val standbyScheduleInterval: Long = 900_000,
    @IntRange(from = 1) private val maxScheduleHangDuration: Long = 300_000,
    private val switcherIntervalSupplier: NumberSupplier =
        ExponentialIncreaseNumberSupplier(600_000, 3600_000),
    private val autoExitStandby: Boolean = false,
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
                handlerScheduler.scheduleDelayed(scheduleInterval)
            }
            ScheduleMode.ALARM -> {
                handlerScheduler.cancel()
                handlerSchedulerRunnable.reset()
                alarmScheduler.scheduleRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + standbyScheduleInterval,
                    standbyScheduleInterval
                )
            }
            ScheduleMode.ALARM_AND_HANDLER -> {
                alarmScheduler.scheduleRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + standbyScheduleInterval,
                    standbyScheduleInterval
                )
                handlerScheduler.cancel()
                handlerSchedulerRunnable.reset()
                handlerScheduler.scheduleDelayed(scheduleInterval)
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
                ScaffoldLogger.debug("[TenaciousScheduler] Scheduler scheduled, SS will arrive after ${switcherInterval / 1000} seconds")
            }
        } else {
            if (ScaffoldLogger.isWarnEnabled()) {
                ScaffoldLogger.warn("[TenaciousScheduler] Repetitive schedule")
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
                ScaffoldLogger.warn("[TenaciousScheduler] Repetitive cancel")
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
            private set
        var maxScheduleInterval: Long? = null
            private set

        override fun run() {
            if (ScheduleMode.NONE == scheduleMode) {
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[TenaciousScheduler] HSR schedule ignored")
                }
                return
            }
            try {
                val logMessageBuilder = StringBuilder()
                val nowTime = SystemClock.elapsedRealtime()
                lastScheduleTime.let { lst ->
                    if (lst == null) {
                        logMessageBuilder.append("[TenaciousScheduler] HSR schedule : first run")
                    } else {
                        val currScheduleInterval = nowTime - lst
                        maxScheduleInterval = maxScheduleInterval?.let { msi ->
                            max(currScheduleInterval, msi)
                        } ?: currScheduleInterval
                        logMessageBuilder.append("[TenaciousScheduler] HSR schedule : currScheduleInterval = $currScheduleInterval, maxScheduleInterval = $maxScheduleInterval")
                    }
                }
                (ScheduleMode.HANDLER == scheduleMode).let {
                    logMessageBuilder.append(" : $it")
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug(logMessageBuilder.toString())
                    }
                    if (it) {
                        runnable.run()
                    }
                }
            } finally {
                lastScheduleTime = SystemClock.elapsedRealtime()
                handlerScheduler.scheduleDelayed(scheduleInterval)
            }
        }

        fun reset() {
            lastScheduleTime = null
            maxScheduleInterval = null
            if (ScaffoldLogger.isDebugEnabled()) {
                ScaffoldLogger.debug("[TenaciousScheduler] HSR reset")
            }
        }

    }

    private inner class AlarmSchedulerRunnable : Runnable {

        override fun run() {
            if (ScheduleMode.NONE == scheduleMode) {
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[TenaciousScheduler] ASR schedule ignored")
                }
                return
            }
            (ScheduleMode.ALARM == scheduleMode || ScheduleMode.ALARM_AND_HANDLER == scheduleMode).let {
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[TenaciousScheduler] ASR schedule : $it")
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
                    ScaffoldLogger.debug("[TenaciousScheduler] SSR schedule ignored")
                }
                return
            }
            val nowTime = SystemClock.elapsedRealtime()
            val handlerLastScheduleTime = handlerSchedulerRunnable.lastScheduleTime
            val handlerMaxScheduleInterval = handlerSchedulerRunnable.maxScheduleInterval
            if (autoExitStandby) {
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
                        ScaffoldLogger.debug("[TenaciousScheduler] SSR schedule : nowTime = $nowTime, nextScheduleInterval = ${switcherInterval / 1000}, HSR_lastTime = $handlerLastScheduleTime, HSR_maxInterval = $handlerMaxScheduleInterval : HS = ON, AS = ON, SS = ON")
                    }
                } else {
                    if (ScheduleMode.HANDLER != scheduleMode) {
                        switchScheduleMode(ScheduleMode.HANDLER)
                    } else {
                        handlerSchedulerRunnable.reset()
                    }
                    val switcherInterval = startSwitchScheduler(false)
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[TenaciousScheduler] SSR schedule : nowTime = $nowTime, nextScheduleInterval = ${switcherInterval / 1000}, HSR_lastTime = $handlerLastScheduleTime, HSR_maxInterval = $handlerMaxScheduleInterval : HS = ON, AS = OFF, SS = ON")
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
                    } else {
                        handlerSchedulerRunnable.reset()
                    }
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[TenaciousScheduler] SSR schedule : nowTime = $nowTime, HSR_lastTime = $handlerLastScheduleTime, HSR_maxInterval = $handlerMaxScheduleInterval : HS = OFF, AS = ON, SS = OFF")
                    }
                } else {
                    handlerSchedulerRunnable.reset()
                    val switcherInterval = startSwitchScheduler(false)
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[TenaciousScheduler] SSR schedule : nowTime = $nowTime, nextScheduleInterval = ${switcherInterval / 1000}, HSR_lastTime = $handlerLastScheduleTime, HSR_maxInterval = $handlerMaxScheduleInterval : HS = ON, AS = OFF, SS = ON")
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
                nowTime - handlerLastScheduleTime > maxScheduleHangDuration
            } else {
                handlerMaxScheduleInterval > maxScheduleHangDuration
            }
        }

    }

    private inner class ScreenBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null || intent == null) return
            if (ScheduleMode.NONE == scheduleMode) {
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[TenaciousScheduler] SBR schedule ignored")
                }
                return
            }
            if (Intent.ACTION_SCREEN_ON == intent.action) {
                if (ScheduleMode.HANDLER != scheduleMode) {
                    switchScheduleMode(ScheduleMode.HANDLER)
                }
                stopSwitchScheduler()
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[TenaciousScheduler] SBR schedule : Screen = ON : HS = ON, AS = OFF, SS = OFF")
                }
            } else if (Intent.ACTION_SCREEN_OFF == intent.action) {
                if (ScheduleMode.HANDLER != scheduleMode) {
                    switchScheduleMode(ScheduleMode.HANDLER)
                }
                val switcherInterval = startSwitchScheduler(true)
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[TenaciousScheduler] SBR schedule : Screen = OFF, nextScheduleInterval = ${switcherInterval / 1000} : HS = ON, AS = OFF, SS = ON")
                }
            } else {
                if (ScaffoldLogger.isWarnEnabled()) {
                    ScaffoldLogger.warn("[TenaciousScheduler] SBR schedule : Illegal action : ${intent.action}")
                }
            }
        }

    }

}