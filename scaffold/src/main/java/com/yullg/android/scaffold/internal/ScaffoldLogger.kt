package com.yullg.android.scaffold.internal

import androidx.annotation.RestrictTo
import com.yullg.android.scaffold.app.ScaffoldConstants
import com.yullg.android.scaffold.support.logger.ILogger
import com.yullg.android.scaffold.support.logger.Logger

@RestrictTo(RestrictTo.Scope.LIBRARY)
object ScaffoldLogger : ILogger by Logger(ScaffoldConstants.Logger.NAME_SCAFFOLD)