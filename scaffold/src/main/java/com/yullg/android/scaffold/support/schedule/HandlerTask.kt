package com.yullg.android.scaffold.support.schedule

import android.os.*

abstract class HandlerTask(looper: Looper) : Runnable {

    private val handler = Handler(looper)

    fun start() = handler.sendMessage(Message.obtain(handler, this))

    fun startAtTime(uptimeMillis: Long) =
        handler.sendMessageAtTime(Message.obtain(handler, this), uptimeMillis)

    fun startDelayed(delayMillis: Long) =
        handler.sendMessageDelayed(Message.obtain(handler, this), delayMillis)

    fun stop() = handler.removeCallbacks(this)

}

abstract class MainHandlerTask : HandlerTask(Looper.getMainLooper())

abstract class ThreadHandlerTask(name: String, priority: Int) :
    HandlerTask(HandlerThread(name, priority).apply { start() }.looper) {

    constructor(name: String) : this(name, Process.THREAD_PRIORITY_DEFAULT)

}