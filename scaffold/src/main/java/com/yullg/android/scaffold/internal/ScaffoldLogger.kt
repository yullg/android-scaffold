package com.yullg.android.scaffold.internal

import androidx.annotation.RestrictTo
import com.yullg.android.scaffold.core.Constants
import com.yullg.android.scaffold.support.logger.ILogger
import com.yullg.android.scaffold.support.logger.Logger

@RestrictTo(RestrictTo.Scope.LIBRARY)
object ScaffoldLogger : ILogger by Logger(Constants.Logger.NAME_SCAFFOLD)