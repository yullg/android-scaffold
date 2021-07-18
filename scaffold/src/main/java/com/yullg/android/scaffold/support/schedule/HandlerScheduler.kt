package com.yullg.android.scaffold.support.schedule

import android.os.*

open class HandlerScheduler(private val handler: Handler, private val runnable: Runnable) {

    constructor(looper: Looper, runnable: Runnable) : this(Handler(looper), runnable)

    fun schedule() = handler.sendMessage(Message.obtain(handler, runnable))

    fun scheduleAtTime(uptimeMillis: Long) =
        handler.sendMessageAtTime(Message.obtain(handler, runnable), uptimeMillis)

    fun scheduleDelayed(delayMillis: Long) =
        handler.sendMessageDelayed(Message.obtain(handler, runnable), delayMillis)

    fun cancel() = handler.removeCallbacks(runnable)

}

open class MainHandlerScheduler(runnable: Runnable) :
    HandlerScheduler(Looper.getMainLooper(), runnable)

open class ThreadHandlerScheduler(name: String, priority: Int, runnable: Runnable) :
    HandlerScheduler(HandlerThread(name, priority).apply { start() }.looper, runnable) {

    constructor(name: String, runnable: Runnable) :
            this(name, Process.THREAD_PRIORITY_DEFAULT, runnable)

}