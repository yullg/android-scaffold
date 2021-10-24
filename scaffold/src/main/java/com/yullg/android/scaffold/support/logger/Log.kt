package com.yullg.android.scaffold.support.logger

import android.os.Process
import java.util.*

enum class LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR, FATAL
}

data class Log(
    val name: String,
    val logLevel: LogLevel,
    val message: Any?,
    val error: Throwable? = null,
    val processId: Int = Process.myPid(),
    val threadId: Long = Thread.currentThread().id,
    val threadName: String = Thread.currentThread().name,
    val time: Date = Date(),
)