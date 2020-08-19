package com.yullg.android.scaffold.support.logger

import java.util.*

enum class LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR, FATAL
}

data class Log(
    val processId: Int,
    val threadId: Long,
    val threadName: String,
    val name: String,
    val logLevel: LogLevel,
    val time: Date,
    val message: Any?,
    val error: Throwable?
)