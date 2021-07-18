package com.yullg.android.scaffold.support.schedule

import android.os.SystemClock
import androidx.annotation.CallSuper
import androidx.work.*
import com.yullg.android.scaffold.app.Scaffold
import com.yullg.android.scaffold.internal.ScaffoldLogger

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
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[DurableDirectAlarmScheduler] Schedule will be filtered because it is too frequent : lastTime = $lastDeliveredTime, nowTime = $nowTime")
                    }
                    return@DirectAlarmScheduler
                }
            } catch (e: Exception) {
                if (ScaffoldLogger.isErrorEnabled()) {
                    ScaffoldLogger.error(
                        "[DurableDirectAlarmScheduler] Scheduling filtering failed",
                        e
                    )
                }
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