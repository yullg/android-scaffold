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
import kotlin.math.min

private const val SCHEDULER_NONE: Int = 0
private const val SCHEDULER_HANDLER: Int = 1
private const val SCHEDULER_ALARM: Int = 2

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

    private var scheduling: Boolean = false
    private var scheduler: Int = SCHEDULER_NONE

    fun schedule() {
        if (!scheduling) {
            scheduling = true
            switchScheduler(SCHEDULER_HANDLER)
            val switcherInterval = scheduleSwitchScheduler(true)
            Scaffold.context.registerReceiver(screenBroadcastReceiver, IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            })
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
        if (scheduling) {
            scheduling = false
            scheduler = SCHEDULER_NONE
            handlerScheduler.cancel()
            alarmScheduler.cancel()
            switcherScheduler.cancel()
            Scaffold.context.unregisterReceiver(screenBroadcastReceiver)
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

    private fun switchScheduler(targetScheduler: Int) {
        if (SCHEDULER_HANDLER == targetScheduler) {
            scheduler = SCHEDULER_HANDLER
            handlerSchedulerRunnable.reset()
            handlerScheduler.cancel()
            handlerScheduler.scheduleDelayed(handlerScheduleInterval)
            alarmScheduler.cancel()
        } else if (SCHEDULER_ALARM == targetScheduler) {
            scheduler = SCHEDULER_ALARM
            alarmScheduler.cancel()
            alarmScheduler.scheduleRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + alarmScheduleInterval,
                alarmScheduleInterval
            )
            handlerScheduler.cancel()
        } else {
            if (ScaffoldLogger.isWarnEnabled()) {
                ScaffoldLogger.warn("[TenaciousScheduler] Scheduler is not supported : $targetScheduler")
            }
        }
    }

    private fun scheduleSwitchScheduler(resetSupplier: Boolean): Long {
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

    private inner class ScreenBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null || intent == null) return
            if (!scheduling) {
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[TenaciousScheduler] SBR execution was interrupted because the schedule was not started")
                }
                return
            }
            switchScheduler(SCHEDULER_HANDLER)
            if (Intent.ACTION_SCREEN_ON == intent.action) {
                switcherScheduler.cancel()
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[TenaciousScheduler] SBR received ON and has performed switchover : HS = ON, AS = OFF, SS = OFF")
                }
            } else if (Intent.ACTION_SCREEN_OFF == intent.action) {
                val switcherInterval = scheduleSwitchScheduler(true)
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[TenaciousScheduler] SBR received OFF and has started SS, SS will run after ${switcherInterval / 1000} seconds : HS = ON, AS = OFF, SS = ON")
                }
            }
        }

    }

    private inner class HandlerSchedulerRunnable : Runnable {

        var lastScheduleTime: Long = -1
        var maxScheduleInterval: Long = Long.MIN_VALUE

        override fun run() {
            if (!scheduling) {
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[TenaciousScheduler] HSR execution was interrupted because the schedule was not started")
                }
                return
            }
            try {
                val nowTime = SystemClock.elapsedRealtime()
                val logMessage = StringBuilder()
                if (lastScheduleTime > 0) {
                    val scheduleInterval = nowTime - lastScheduleTime
                    if (scheduleInterval > maxScheduleInterval) {
                        if (ScaffoldLogger.isDebugEnabled()) {
                            logMessage.append("[TenaciousScheduler] HSR start delivery : scheduleInterval = $scheduleInterval, maxScheduleInterval = $maxScheduleInterval")
                        }
                        maxScheduleInterval = scheduleInterval
                    } else {
                        if (ScaffoldLogger.isDebugEnabled()) {
                            logMessage.append("[TenaciousScheduler] HSR start delivery : scheduleInterval = $scheduleInterval, maxScheduleInterval = $maxScheduleInterval")
                        }
                    }
                } else {
                    if (ScaffoldLogger.isDebugEnabled()) {
                        logMessage.append("[TenaciousScheduler] HSR start delivery : first run")
                    }
                }
                (SCHEDULER_HANDLER == scheduler).let {
                    if (ScaffoldLogger.isDebugEnabled()) {
                        logMessage.append(", $it")
                        ScaffoldLogger.debug(logMessage.toString())
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
            lastScheduleTime = -1
            maxScheduleInterval = Long.MIN_VALUE
            if (ScaffoldLogger.isDebugEnabled()) {
                ScaffoldLogger.debug("[TenaciousScheduler] HSR has been reset")
            }
        }

    }

    private inner class AlarmSchedulerRunnable : Runnable {

        override fun run() {
            if (!scheduling) {
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[TenaciousScheduler] ASR execution was interrupted because the schedule was not started")
                }
                return
            }
            (SCHEDULER_ALARM == scheduler).let {
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[TenaciousScheduler] ASR start delivery : $it")
                }
                if (it) {
                    runnable.run()
                }
            }
        }

    }

    private inner class SwitcherSchedulerRunnable : Runnable {

        override fun run() {
            if (!scheduling) {
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
                    switchScheduler(SCHEDULER_ALARM)
                    handlerSchedulerRunnable.reset()
                    handlerScheduler.scheduleDelayed(handlerScheduleInterval)
                    val switcherInterval = scheduleSwitchScheduler(false)
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[TenaciousScheduler] SSR has performed switchover (nowTime = $nowTime, lastTime = $handlerLastScheduleTime, maxInterval = $handlerMaxScheduleInterval) and will rerun after ${switcherInterval / 1000} seconds : HS = ON, AS = ON, SS = ON")
                    }
                } else {
                    switchScheduler(SCHEDULER_HANDLER)
                    val switcherInterval = scheduleSwitchScheduler(false)
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
                    switchScheduler(SCHEDULER_ALARM)
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[TenaciousScheduler] SSR has performed switchover (nowTime = $nowTime, lastTime = $handlerLastScheduleTime, maxInterval = $handlerMaxScheduleInterval) : HS = OFF, AS = ON, SS = OFF")
                    }
                } else {
                    handlerSchedulerRunnable.reset()
                    val switcherInterval = scheduleSwitchScheduler(false)
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[TenaciousScheduler] SSR did not perform switchover (nowTime = $nowTime, lastTime = $handlerLastScheduleTime, maxInterval = $handlerMaxScheduleInterval) and will rerun after ${switcherInterval / 1000} seconds : HS = ON, AS = OFF, SS = ON")
                    }
                }
            }
        }

        private fun shouldSwitchToAlarmScheduler(
            nowTime: Long,
            handlerLastScheduleTime: Long,
            handlerMaxScheduleInterval: Long
        ): Boolean {
            if (handlerLastScheduleTime <= 0 || handlerMaxScheduleInterval > maxHandlerScheduleIdleDuration) {
                return true
            }
            if (nowTime - handlerLastScheduleTime > maxHandlerScheduleIdleDuration) {
                return true
            }
            return false
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