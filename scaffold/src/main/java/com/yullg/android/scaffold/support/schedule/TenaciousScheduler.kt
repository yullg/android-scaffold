package com.yullg.android.scaffold.support.schedule

import android.app.AlarmManager
import android.os.SystemClock
import androidx.annotation.CallSuper
import androidx.annotation.IntRange
import com.yullg.android.scaffold.core.DeviceInteractiveStateObserver
import com.yullg.android.scaffold.core.ExponentialIncreaseNumberSupplier
import com.yullg.android.scaffold.core.NumberSupplier
import com.yullg.android.scaffold.internal.ScaffoldLogger
import kotlin.math.max

private enum class ScheduleMode { NONE, HANDLER, ALARM, ALARM_AND_HANDLER }

/**
 * 组合多个调度器来实现稳定的任务调度功能，减少受系统省电策略的影响。
 *
 * 该类组合一个基于[android.os.Handler]的调度器（以下称为A调度器）和两个基于[AlarmManager]的调度器（以下称为B、C调度器）：
 * *  A调度器每[scheduleInterval]毫秒触发并调用[runnable]一次。受系统省电策略影响，这个调度器在系统休眠后可能会被挂起。
 * *  B调度器至少每[enhancedScheduleInterval]毫秒触发并调用[runnable]一次。因为是基于[AlarmManager]的非精确方式调度任务，所以触发时间不可控
 *   （[enhancedScheduleInterval]仅表示最小间隔）。调度器默认在系统休眠状态下唤醒系统来执行任务，可以通过[enhancedSchedulerPunctuality]参数关闭，关闭后将推迟到下个window。
 * *  C调度器负责在A、B两个调度器之间切换，当A调度器不可用时切换到B调度器，当A调度器可用时再切换回去。它的触发时间由[switcherIntervalSupplier]控制，和B调度器一样，触发时间不可控
 *   （[switcherIntervalSupplier]仅表示最小间隔）。调度器默认在系统休眠状态下唤醒系统来执行切换操作，可以通过[switcherSchedulerPunctuality]参数关闭，关闭后将推迟到下个window。
 *    当切换到B调度器时，如果[autoExitEnhance]设置为false（默认为false），那么就同时关闭A调度器，否则将保持A调度器开启，但在触发时不会调用[runnable]，而是仅用于记录调度间隔，
 *    以便用于判断A调度器是否又变为可用状态。判断A调度器是否可用的条件是：如果设备处于不可交互状态并且A调度器最大一次的调度间隔大于[maxScheduleHangDuration]毫秒，则认为A调度器不可用，否则认为A调度器可用。
 *
 * 当[schedule()]方法被调用后，将启动A调度器，并且开始监听设备的交互状态。如果设备是不可交互的，那么就启动C调度器，否则就关闭C调度器。
 * 当[cancel()]方法被调用后，将取消所有调度器，并且不再监听设备的交互状态。
 *
 * 调用[close()]将停止调度并且实例销毁不可重用。
 */
open class TenaciousScheduler(
    private val uniqueName: String,
    @IntRange(from = 1) private val scheduleInterval: Long = 5_000,
    @IntRange(from = 1) private val maxScheduleHangDuration: Long = 300_000,
    @IntRange(from = 60000) private val enhancedScheduleInterval: Long = 900_000,
    private val switcherIntervalSupplier: NumberSupplier =
        ExponentialIncreaseNumberSupplier(600_000, 9600_000),
    private val enhancedSchedulerPunctuality: Boolean = true,
    private val switcherSchedulerPunctuality: Boolean = true,
    private val autoExitEnhance: Boolean = false,
    private val runnable: Runnable
) : AutoCloseable {

    private val handlerSchedulerRunnable: HandlerSchedulerRunnable
    private val alarmSchedulerRunnable: AlarmSchedulerRunnable
    private val switcherSchedulerRunnable: SwitcherSchedulerRunnable
    private val handlerScheduler: MainHandlerScheduler
    private val alarmScheduler: DirectAlarmScheduler
    private val switcherScheduler: DirectAlarmScheduler
    private val deviceInteractiveStateObserver: DeviceInteractiveStateObserver

    private var scheduleMode: ScheduleMode = ScheduleMode.NONE

    init {
        handlerSchedulerRunnable = HandlerSchedulerRunnable()
        alarmSchedulerRunnable = AlarmSchedulerRunnable()
        switcherSchedulerRunnable = SwitcherSchedulerRunnable()
        handlerScheduler = MainHandlerScheduler(handlerSchedulerRunnable)
        alarmScheduler = DirectAlarmScheduler(uniqueName, alarmSchedulerRunnable)
        switcherScheduler = DirectAlarmScheduler(
            uniqueName + "_switcher",
            switcherSchedulerRunnable
        )
        deviceInteractiveStateObserver = DeviceInteractiveStateObserver { isInteractive ->
            try {
                // 当设备的交互状态改变时执行以下操作：
                if (ScheduleMode.NONE == scheduleMode) {
                    // 如果当前调度器未开始或已取消，那么忽略设备的交互状态改变事件。正常情况下不太可能出现这种情况，
                    // 因为只有在调度器开始时才监听设备的交互状态，并且在调度器取消时同时取消监听。但还是有必要预防
                    // 一下监听器触发导致A、B调度器意外启动。
                    ScaffoldLogger.warn("[TenaciousScheduler] Interactive state changed : ignored")
                    return@DeviceInteractiveStateObserver
                }
                if (ScheduleMode.HANDLER != scheduleMode) {
                    // 当设备的交互状态发生改变，保证处于HANDLER模式（A调度器启用，B调度器停用）
                    switchScheduleMode(ScheduleMode.HANDLER)
                }
                if (isInteractive) {
                    // 如果设备是可交互的，那么A调度器应该可以稳定运行，不需要启动C调度器
                    cancelSwitcher()
                    ScaffoldLogger.debug("[TenaciousScheduler] Interactive state changed : Interactive = ON, HS = ON, AS = OFF, SS = OFF")
                } else {
                    // 如果设备是不可交互的，那么A调度器可能被挂起，需要启动C调度器执行切换任务
                    // 这是C调度器在设备变更为不可交互状态后第一次安排任务，需要重置C调度器的执行间隔
                    val switcherInterval = scheduleSwitcher(true)
                    ScaffoldLogger.debug("[TenaciousScheduler] Interactive state changed : Interactive = OFF, SwitcherInterval = ${switcherInterval / 1000}, HS = ON, AS = OFF, SS = ON")
                }
            } catch (e: Exception) {
                ScaffoldLogger.error(
                    "[TenaciousScheduler] Interactive state changed : Error",
                    e
                )
            }
        }
    }

    fun schedule() {
        if (ScheduleMode.NONE == scheduleMode) {
            switchScheduleMode(ScheduleMode.HANDLER)
            deviceInteractiveStateObserver.mount()
            ScaffoldLogger.debug("[TenaciousScheduler] Schedule succeeded")
        } else {
            ScaffoldLogger.warn("[TenaciousScheduler] Schedule ignored")
        }
    }

    fun cancel() {
        if (ScheduleMode.NONE != scheduleMode) {
            deviceInteractiveStateObserver.unmount()
            cancelSwitcher()
            switchScheduleMode(ScheduleMode.NONE)
            ScaffoldLogger.debug("[TenaciousScheduler] Cancel succeeded")
        } else {
            ScaffoldLogger.warn("[TenaciousScheduler] Cancel ignored")
        }
    }

    @CallSuper
    override fun close() {
        cancel()
        alarmScheduler.close()
        switcherScheduler.close()
        ScaffoldLogger.debug("[TenaciousScheduler] Close succeeded")
    }

    /**
     * 安排C调度器在将来的某个时间执行一次，如果[resetSupplier]设置为true，则重置调度间隔提供者
     * （如果提供者提供的间隔时间是递增的，那么它会重新开始累计）。注意每次安排C调度器任务都
     * 会清除A调度器记录的最大调度间隔，使其重新记录。
     * @return 执行下个任务的最小间隔时间（毫秒）
     */
    private fun scheduleSwitcher(resetSupplier: Boolean): Long {
        if (resetSupplier) {
            switcherIntervalSupplier.reset()
        }
        val switcherInterval = switcherIntervalSupplier.get()
        handlerSchedulerRunnable.reset()
        switcherScheduler.scheduleAndAllowWhileIdle(
            if (switcherSchedulerPunctuality) AlarmManager.ELAPSED_REALTIME_WAKEUP else AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + switcherInterval
        )
        return switcherInterval
    }

    /**
     * 取消待执行的C调度器任务
     */
    private fun cancelSwitcher() {
        switcherScheduler.cancel()
    }

    /**
     * 切换调度器模式，分别启用或停用A、B调度器。
     * *  [ScheduleMode.HANDLER]：A = 启用，B = 停用
     * *  [ScheduleMode.ALARM]：A = 停用，B = 启用
     * *  [ScheduleMode.ALARM_AND_HANDLER]：A = 启用，B = 启用
     * *  [ScheduleMode.NONE]：A = 停用，B = 停用
     */
    private fun switchScheduleMode(scheduleMode: ScheduleMode) {
        this.scheduleMode = scheduleMode
        when (scheduleMode) {
            ScheduleMode.HANDLER -> {
                alarmScheduler.cancel()
                handlerScheduler.cancel()
                handlerScheduler.scheduleDelayed(scheduleInterval)
            }
            ScheduleMode.ALARM -> {
                handlerScheduler.cancel()
                alarmScheduler.scheduleRepeating(
                    if (enhancedSchedulerPunctuality) AlarmManager.ELAPSED_REALTIME_WAKEUP else AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + enhancedScheduleInterval,
                    enhancedScheduleInterval
                )
            }
            ScheduleMode.ALARM_AND_HANDLER -> {
                alarmScheduler.scheduleRepeating(
                    if (enhancedSchedulerPunctuality) AlarmManager.ELAPSED_REALTIME_WAKEUP else AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + enhancedScheduleInterval,
                    enhancedScheduleInterval
                )
                handlerScheduler.cancel()
                handlerScheduler.scheduleDelayed(scheduleInterval)
            }
            ScheduleMode.NONE -> {
                alarmScheduler.cancel()
                handlerScheduler.cancel()
            }
        }
    }

    private inner class HandlerSchedulerRunnable : Runnable {

        var lastScheduleTime: Long? = null
            private set
        var maxScheduleInterval: Long? = null
            private set

        override fun run() {
            if (ScheduleMode.NONE == scheduleMode) {
                ScaffoldLogger.debug("[TenaciousScheduler] HSR schedule : ignored")
                return
            }
            try {
                val logMessageBuilder = StringBuilder()
                val nowTime = SystemClock.elapsedRealtime()
                lastScheduleTime.let { lst ->
                    if (lst == null) {
                        // 任务是（重）新运行，还没有上次执行的结束时间记录
                        logMessageBuilder.append("[TenaciousScheduler] HSR schedule : first run")
                    } else {
                        // 更新最大调度间隔
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
                // 记录此次任务执行的结束时间，用于在下次任务执行前计算调度间隔
                lastScheduleTime = SystemClock.elapsedRealtime()
                handlerScheduler.scheduleDelayed(scheduleInterval)
            }
        }

        fun reset() {
            lastScheduleTime = null
            maxScheduleInterval = null
            ScaffoldLogger.debug("[TenaciousScheduler] HSR reset")
        }

    }

    private inner class AlarmSchedulerRunnable : Runnable {

        override fun run() {
            if (ScheduleMode.NONE == scheduleMode) {
                ScaffoldLogger.debug("[TenaciousScheduler] ASR schedule : ignored")
                return
            }
            (ScheduleMode.ALARM == scheduleMode || ScheduleMode.ALARM_AND_HANDLER == scheduleMode).let {
                ScaffoldLogger.debug("[TenaciousScheduler] ASR schedule : $it")
                if (it) {
                    runnable.run()
                }
            }
        }

    }

    private inner class SwitcherSchedulerRunnable : Runnable {

        override fun run() {
            if (ScheduleMode.NONE == scheduleMode) {
                ScaffoldLogger.debug("[TenaciousScheduler] SSR schedule : ignored")
                return
            }
            val nowTime = SystemClock.elapsedRealtime()
            val handlerLastScheduleTime = handlerSchedulerRunnable.lastScheduleTime
            val handlerMaxScheduleInterval = handlerSchedulerRunnable.maxScheduleInterval
            if (autoExitEnhance) {
                if (shouldSwitchToAlarmScheduler(
                        nowTime,
                        handlerLastScheduleTime,
                        handlerMaxScheduleInterval
                    )
                ) {
                    if (ScheduleMode.ALARM_AND_HANDLER != scheduleMode) {
                        // C调度器需要处理由B调度器到A调度器的切换，所以它在切换到B调度器后不能停止A调度器，需要A调度器一直记录执行间隔
                        switchScheduleMode(ScheduleMode.ALARM_AND_HANDLER)
                    }
                    // 安排C调度器下次的任务，不需要重置执行间隔
                    val switcherInterval = scheduleSwitcher(false)
                    ScaffoldLogger.debug("[TenaciousScheduler] SSR schedule : nowTime = $nowTime, nextScheduleInterval = ${switcherInterval / 1000}, HSR_lastTime = $handlerLastScheduleTime, HSR_maxInterval = $handlerMaxScheduleInterval : HS = ON, AS = ON, SS = ON")
                } else {
                    if (ScheduleMode.HANDLER != scheduleMode) {
                        // A调度器又可用了，切换回去...
                        switchScheduleMode(ScheduleMode.HANDLER)
                    }
                    // 安排C调度器下次的任务，不需要重置执行间隔
                    val switcherInterval = scheduleSwitcher(false)
                    ScaffoldLogger.debug("[TenaciousScheduler] SSR schedule : nowTime = $nowTime, nextScheduleInterval = ${switcherInterval / 1000}, HSR_lastTime = $handlerLastScheduleTime, HSR_maxInterval = $handlerMaxScheduleInterval : HS = ON, AS = OFF, SS = ON")
                }
            } else {
                if (shouldSwitchToAlarmScheduler(
                        nowTime,
                        handlerLastScheduleTime,
                        handlerMaxScheduleInterval
                    )
                ) {
                    if (ScheduleMode.ALARM != scheduleMode) {
                        // C调度器不需要处理由B调度器到A调度器的切换，所以它可以停止A调度器
                        switchScheduleMode(ScheduleMode.ALARM)
                    }
                    // 也不需要安排C调度器再执行了，当前设备交互状态下C调度器的生命周期已终止
                    ScaffoldLogger.debug("[TenaciousScheduler] SSR schedule : nowTime = $nowTime, HSR_lastTime = $handlerLastScheduleTime, HSR_maxInterval = $handlerMaxScheduleInterval : HS = OFF, AS = ON, SS = OFF")
                } else {
                    // 此次检查A调度器正常运行，没有切换操作，安排C调度器下次的任务，不需要重置执行间隔
                    val switcherInterval = scheduleSwitcher(false)
                    ScaffoldLogger.debug("[TenaciousScheduler] SSR schedule : nowTime = $nowTime, nextScheduleInterval = ${switcherInterval / 1000}, HSR_lastTime = $handlerLastScheduleTime, HSR_maxInterval = $handlerMaxScheduleInterval : HS = ON, AS = OFF, SS = ON")
                }
            }
        }

        /**
         * 判断是否需要从A调度器切换到B调度器
         */
        private fun shouldSwitchToAlarmScheduler(
            nowTime: Long,
            handlerLastScheduleTime: Long?,
            handlerMaxScheduleInterval: Long?
        ): Boolean {
            return if (handlerLastScheduleTime == null) {
                // A调度器在此次检查时还从未运行过
                true
            } else if (handlerMaxScheduleInterval == null) {
                // A调度器仅运行过一次，没有记录最大调度间隔，使用当前时间参与计算
                nowTime - handlerLastScheduleTime > maxScheduleHangDuration
            } else {
                // 除了判断最大调度间隔还需要判断最后执行时间，可能最后一次执行到当前时间的间隔已经满足了切换条件
                handlerMaxScheduleInterval > maxScheduleHangDuration
                        || nowTime - handlerLastScheduleTime > maxScheduleHangDuration
            }
        }

    }

}