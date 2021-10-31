package com.yullg.android.scaffold.support.schedule

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.app.job.JobService
import androidx.annotation.CallSuper
import kotlinx.coroutines.*

/**
 * 一个JobService实现，它提供了与Kotlin协程的互操作。重写doWork函数来执行挂起工作。
 *
 * 默认情况下，[CoroutineJobService]运行在[Dispatchers.Default];这可以通过重写[jobScope]来修改。
 */
@SuppressLint("SpecifyJobSchedulerIdRange")
abstract class CoroutineJobService : JobService() {

    protected open val jobScope = CoroutineScope(Dispatchers.Default)

    // 一个JobService实例可以同时运行多个具有不同ID的任务，在这里保存每个任务启动的协程
    private val jobs = HashMap<Int, Job>(2)

    final override fun onStartJob(params: JobParameters): Boolean {
        val job = jobScope.launch {
            doWork(params)
        }
        jobs[params.jobId] = job
        // 注册协程监听器，在协程结束时删除保存在实例变量jobs中的引用
        job.invokeOnCompletion {
            jobs.remove(params.jobId)
        }
        return true
    }

    final override fun onStopJob(params: JobParameters): Boolean {
        jobs[params.jobId]?.cancel()
        return false
    }

    @CallSuper
    override fun onDestroy() {
        jobScope.cancel()
        super.onDestroy()
    }

    /**
     * 在协程中执行任务
     *
     * 当任务开始执行时系统代表应用持有唤醒锁，直到调用[jobFinished()]告诉系统任务已结束，或者因不满足约束条件被系统中断。
     * 当任务被系统中断时，相关联的协程将被取消，此时不需要再调用[jobFinished()]。
     */
    abstract suspend fun doWork(params: JobParameters)

}