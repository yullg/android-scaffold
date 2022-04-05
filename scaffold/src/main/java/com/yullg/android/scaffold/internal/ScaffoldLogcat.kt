package com.yullg.android.scaffold.internal

import android.util.Log
import androidx.annotation.RestrictTo
import com.yullg.android.scaffold.app.ScaffoldConstants

/**
 * 框架专用日志记录器
 *
 * 如果日志没有价值或者是由日志模块自身产生的日志，通常需要通过此记录器将日志直接写入Logcat。
 * 由此记录器写入的日志将使用统一的TAG，由[ScaffoldConstants.Logger.TAG_SCAFFOLD]定义。
 *
 * @see Log
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
object ScaffoldLogcat {

    fun v(msg: String?, tr: Throwable? = null) {
        Log.v(ScaffoldConstants.Logger.TAG_SCAFFOLD, msg, tr)
    }

    fun d(msg: String?, tr: Throwable? = null) {
        Log.d(ScaffoldConstants.Logger.TAG_SCAFFOLD, msg, tr)
    }

    fun i(msg: String?, tr: Throwable? = null) {
        Log.i(ScaffoldConstants.Logger.TAG_SCAFFOLD, msg, tr)
    }

    fun w(msg: String?, tr: Throwable? = null) {
        Log.w(ScaffoldConstants.Logger.TAG_SCAFFOLD, msg, tr)
    }

    fun e(msg: String?, tr: Throwable? = null) {
        Log.e(ScaffoldConstants.Logger.TAG_SCAFFOLD, msg, tr)
    }

    fun wtf(msg: String?, tr: Throwable? = null) {
        Log.wtf(ScaffoldConstants.Logger.TAG_SCAFFOLD, msg, tr)
    }

}