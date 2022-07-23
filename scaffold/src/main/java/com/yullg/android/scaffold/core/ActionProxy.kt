package com.yullg.android.scaffold.core

import android.os.SystemClock

/**
 * 动作代理接口，在最终要执行的动作之上添加一个控制层。
 */
interface ActionProxy<T, R> {

    fun run(action: () -> T): R

}

/**
 * 提供节流功能的[ActionProxy]实现
 *
 * 当请求执行给定的动作时，如果距上次请求的间隔时长小于[interval]，那么请求将被拒绝，动作不会执行而是直接返回NULL,
 * 否则执行给定的动作，并返回它的结果。
 */
open class ThrottledActionProxy<T>(private val interval: Long) : ActionProxy<T, T?> {

    private var lastRunTime: Long = 0

    override fun run(action: () -> T): T? {
        return if (intercept()) null else action()
    }

    /**
     * 检查是否应该拦截动作
     */
    protected fun intercept(): Boolean = synchronized(this) {
        SystemClock.elapsedRealtime().let {
            val result = it - lastRunTime <= interval
            if (!result) {
                lastRunTime = it
            }
            return result
        }
    }

}

/**
 * 一个[ActionProxy]实现，它在连续请求两次执行动作后开始执行动作一次。
 *
 * [interval]属性控制两次请求的最大间隔时长，只有当小于此时长时才判定为连连续请求，否则视为一个新的请求。
 * 当连续请求发现时执行动作一次，并且重新计时，不管下一次请求何时发生，都视为一个新的请求。拦截的请求直接返回NULL。
 */
open class DoubleActionProxy<T>(private val interval: Long) : ActionProxy<T, T?> {

    private var lastRunTime: Long = 0

    override fun run(action: () -> T): T? {
        return if (intercept()) null else action()
    }

    protected fun intercept(): Boolean = synchronized(this) {
        SystemClock.elapsedRealtime().let {
            val result = it - lastRunTime >= interval
            lastRunTime = if (result) it else 0
            return result
        }
    }

}