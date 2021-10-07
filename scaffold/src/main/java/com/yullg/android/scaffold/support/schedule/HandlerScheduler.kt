package com.yullg.android.scaffold.support.schedule

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Process

/**
 * 提供基于[Handler]的任务调度功能。在实例构造时将[Handler]和要执行的任务[Runnable]固定下来，实例仅控制何时执行任务。
 */
open class HandlerScheduler(private val handler: Handler, private val runnable: Runnable) {

    constructor(looper: Looper, runnable: Runnable) : this(Handler(looper), runnable)

    fun schedule() = handler.post(runnable)

    fun scheduleAtTime(uptimeMillis: Long) = handler.postAtTime(runnable, uptimeMillis)

    fun scheduleDelayed(delayMillis: Long) = handler.postDelayed(runnable, delayMillis)

    fun cancel() = handler.removeCallbacks(runnable)

}

/**
 * `MainThread`版本的[HandlerScheduler]
 */
open class MainHandlerScheduler(runnable: Runnable) :
    HandlerScheduler(Looper.getMainLooper(), runnable)

/**
 * `Thread`版本的[HandlerScheduler]
 */
open class ThreadHandlerScheduler(name: String, priority: Int, runnable: Runnable) :
    HandlerScheduler(HandlerThread(name, priority).apply { start() }.looper, runnable) {

    constructor(name: String, runnable: Runnable) :
            this(name, Process.THREAD_PRIORITY_DEFAULT, runnable)

}