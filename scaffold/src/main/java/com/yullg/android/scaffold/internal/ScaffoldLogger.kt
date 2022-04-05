package com.yullg.android.scaffold.internal

import androidx.annotation.RestrictTo
import com.yullg.android.scaffold.app.ScaffoldConstants
import com.yullg.android.scaffold.support.logger.ILogger
import com.yullg.android.scaffold.support.logger.Logger

/**
 * 框架专用日志记录器，日志将发送给内置的日志模块处理。
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
object ScaffoldLogger : ILogger by Logger(ScaffoldConstants.Logger.NAME_SCAFFOLD)