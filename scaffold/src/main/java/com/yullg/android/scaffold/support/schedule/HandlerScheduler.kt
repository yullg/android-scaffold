package com.yullg.android.scaffold.support.schedule

import android.os.*

open class HandlerScheduler(protected val handler: Handler, protected val runnable: Runnable) {

    constructor(looper: Looper, runnable: Runnable) : this(Handler(looper), runnable)

    open fun schedule() = handler.sendMessage(Message.obtain(handler, runnable))

    open fun scheduleAtTime(uptimeMillis: Long) =
        handler.sendMessageAtTime(Message.obtain(handler, runnable), uptimeMillis)

    open fun scheduleDelayed(delayMillis: Long) =
        handler.sendMessageDelayed(Message.obtain(handler, runnable), delayMillis)

    open fun cancel() = handler.removeCallbacks(runnable)

}

open class MainHandlerScheduler(runnable: Runnable) :
    HandlerScheduler(Looper.getMainLooper(), runnable)

open class ThreadHandlerScheduler(name: String, priority: Int, runnable: Runnable) :
    HandlerScheduler(HandlerThread(name, priority).apply { start() }.looper, runnable) {

    constructor(name: String, runnable: Runnable) :
            this(name, Process.THREAD_PRIORITY_DEFAULT, runnable)

}