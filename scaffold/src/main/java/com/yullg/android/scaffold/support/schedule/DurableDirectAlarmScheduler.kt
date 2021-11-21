package com.yullg.android.scaffold.support.schedule

import android.os.SystemClock
import androidx.annotation.CallSuper
import androidx.work.*
import com.yullg.android.scaffold.app.Scaffold
import com.yullg.android.scaffold.internal.ScaffoldLogger

/**
 * 提供基于[android.app.AlarmManager]的任务调度功能。在实例构造时将要执行的任务[ListenableWorker]固定下来，实例仅控制何时执行任务。
 *
 * 该类通过将任务转换给[WorkManager]来执行耗时操作。
 * 当调用[scheduleRepeating()]安排重复任务时，任务可能被推迟，跳过的任务将尽快交付。之后，未来任务将按照原计划进行交付；它们不会随着时间的推移而漂移。
 * 这时可能需要通过[minDeliverIntervalMillis]来过滤掉相隔时间太短的任务，默认过滤间隔小于3秒的任务。
 *
 * 调用[close()]将取消已安排的所有任务并且实例销毁不可重用。
 */
open class DurableDirectAlarmScheduler(
    private val uniqueName: String,
    private val workerClass: Class<out ListenableWorker>
) : AutoCloseable {

    var minDeliverIntervalMillis: Long = 3000

    private val alarmScheduler: DirectAlarmScheduler
    private var lastDeliveredTime: Long = 0

    init {
        alarmScheduler = DirectAlarmScheduler(uniqueName) {
            val nowTime = SystemClock.elapsedRealtime()
            try {
                if (lastDeliveredTime + minDeliverIntervalMillis > nowTime) {
                    ScaffoldLogger.debug("[DurableDirectAlarmScheduler] Schedule will be filtered because it is too frequent : lastTime = $lastDeliveredTime, nowTime = $nowTime, minDeliverIntervalMillis = $minDeliverIntervalMillis")
                    return@DirectAlarmScheduler
                }
            } catch (e: Exception) {
                ScaffoldLogger.error(
                    "[DurableDirectAlarmScheduler] Schedule filtering failed",
                    e
                )
            }
            lastDeliveredTime = nowTime
            enqueueWork()
        }
    }

    fun canScheduleExactAlarms() = alarmScheduler.canScheduleExactAlarms()

    fun schedule(type: Int, triggerAtMillis: Long) = alarmScheduler.schedule(type, triggerAtMillis)

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
        alarmScheduler.use {
            cancel()
        }
    }

    private fun enqueueWork() {
        val workRequest = OneTimeWorkRequest.Builder(workerClass)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        WorkManager.getInstance(Scaffold.context).enqueueUniqueWork(
            uniqueName,
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }

}