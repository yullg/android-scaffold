package com.yullg.android.scaffold.core

import android.os.SystemClock

interface ActionProxy<T> {

    fun run(action: () -> T): T

}

class ThrottledActionProxy<T>(
    private val interval: Long,
    private val defOnIntercepted: ((Long) -> T)
) : ActionProxy<T> {

    private var lastRunTime: Long = 0

    override fun run(action: () -> T) = throttledRun(action)

    fun throttledRun(action: () -> T, onIntercepted: ((Long) -> T)? = null): T {
        val pair = intercept()
        return if (pair.first) {
            onIntercepted?.invoke(pair.second) ?: defOnIntercepted(pair.second)
        } else {
            action()
        }
    }

    private fun intercept(): Pair<Boolean, Long> = synchronized(this) {
        SystemClock.elapsedRealtime().let {
            val currentInterval = it - lastRunTime
            return if (currentInterval >= interval) {
                lastRunTime = it
                Pair(false, currentInterval)
            } else {
                Pair(true, currentInterval)
            }
        }
    }

}

class DoubleActionProxy<T>(
    private val interval: Long,
    private val defOnIntercepted: ((Long) -> T)
) : ActionProxy<T> {

    private var lastRunTime: Long = 0

    override fun run(action: () -> T) = doubleRun(action, null)

    fun doubleRun(action: () -> T, onIntercepted: ((Long) -> T)? = null): T {
        val pair = intercept()
        return if (pair.first) {
            onIntercepted?.invoke(pair.second) ?: defOnIntercepted(pair.second)
        } else {
            action()
        }
    }

    private fun intercept(): Pair<Boolean, Long> = synchronized(this) {
        SystemClock.elapsedRealtime().let {
            val currentInterval = it - lastRunTime
            lastRunTime = it
            return if (currentInterval >= interval) {
                Pair(true, currentInterval)
            } else {
                Pair(false, currentInterval)
            }
        }
    }

}