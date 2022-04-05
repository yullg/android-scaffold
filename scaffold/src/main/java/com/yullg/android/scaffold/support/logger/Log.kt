package com.yullg.android.scaffold.support.logger

import android.os.Process
import com.yullg.android.scaffold.support.logger.LogLevel.*
import java.io.File
import java.util.*

/**
 * 日志级别，从低到高依次为：[TRACE] -> [DEBUG] -> [INFO] -> [WARN] -> [ERROR] -> [FATAL]。
 */
enum class LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR, FATAL
}

/**
 * 日志
 */
data class Log(
    val name: String,
    val level: LogLevel,
    val message: Any?,
    val error: Throwable? = null,
    val processId: Int = Process.myPid(),
    val threadId: Long = Thread.currentThread().id,
    val threadName: String = Thread.currentThread().name,
    val time: Date = Date(),
)

/**
 * 日志文件
 */
data class LogFile(
    val name: String,
    val time: Date,
    val file: File
)