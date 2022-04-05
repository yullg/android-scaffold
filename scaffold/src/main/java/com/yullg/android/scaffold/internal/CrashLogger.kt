package com.yullg.android.scaffold.internal

import androidx.annotation.RestrictTo
import com.yullg.android.scaffold.app.ScaffoldConstants
import com.yullg.android.scaffold.support.logger.ILogger
import com.yullg.android.scaffold.support.logger.Logger

/**
 * 崩溃日志记录器，此记录器使用同步方式记录日志。
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
object CrashLogger : ILogger by Logger(ScaffoldConstants.Logger.NAME_CRASH, true)