package com.yullg.android.scaffold.ui.util

import android.os.SystemClock

interface ActionProxy {

    fun run(action: () -> Unit)

}

open class ThrottledActionProxy(private val interval: Int) : ActionProxy {

    protected var lastTime: Long = 0

    override fun run(action: () -> Unit) {
        if (test()) return
        action()
    }

    protected open fun test(): Boolean {
        synchronized(this) {
            SystemClock.elapsedRealtime().let {
                if (it - lastTime < interval) {
                    return true
                }
                lastTime = it
            }
        }
        return false
    }

}