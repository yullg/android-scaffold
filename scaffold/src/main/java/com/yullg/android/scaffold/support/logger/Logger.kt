package com.yullg.android.scaffold.support.logger

import android.os.Process
import com.yullg.android.scaffold.core.Constants
import java.util.*

class Logger(private val name: String) : ILogger {

    override fun log(logLevel: LogLevel, message: Any?, error: Throwable?) {
        Appender.writeLog(
            Log(
                processId = Process.myPid(),
                threadId = Thread.currentThread().id,
                threadName = Thread.currentThread().name,
                name = name,
                logLevel = logLevel,
                time = Date(),
                message = message,
                error = error
            )
        )
    }

    override fun isEnabled(logLevel: LogLevel): Boolean =
        (LoggerConfig.findConsoleAppenderEnabled(name)
                && LoggerConfig.findConsoleAppenderLevel(name) <= logLevel)
                || (LoggerConfig.findFileAppenderEnabled(name)
                && LoggerConfig.findFileAppenderLevel(name) <= logLevel)

    companion object : ILogger by Logger(Constants.Logger.NAME_DEFAULT)

}

interface ILogger {

    fun trace(message: Any?) = trace(message, null)

    fun trace(message: Any?, error: Throwable?) = log(LogLevel.TRACE, message, error)

    fun debug(message: Any?) = debug(message, null)

    fun debug(message: Any?, error: Throwable?) = log(LogLevel.DEBUG, message, error)

    fun info(message: Any?) = info(message, null)

    fun info(message: Any?, error: Throwable?) = log(LogLevel.INFO, message, error)

    fun warn(message: Any?) = warn(message, null)

    fun warn(message: Any?, error: Throwable?) = log(LogLevel.WARN, message, error)

    fun error(message: Any?) = error(message, null)

    fun error(message: Any?, error: Throwable?) = log(LogLevel.ERROR, message, error)

    fun fatal(message: Any?) = fatal(message, null)

    fun fatal(message: Any?, error: Throwable?) = log(LogLevel.FATAL, message, error)

    fun isTraceEnabled() = isEnabled(LogLevel.TRACE)

    fun isDebugEnabled() = isEnabled(LogLevel.DEBUG)

    fun isInfoEnabled() = isEnabled(LogLevel.INFO)

    fun isWarnEnabled() = isEnabled(LogLevel.WARN)

    fun isErrorEnabled() = isEnabled(LogLevel.ERROR)

    fun isFatalEnabled() = isEnabled(LogLevel.FATAL)

    fun log(logLevel: LogLevel, message: Any?, error: Throwable?)

    fun isEnabled(logLevel: LogLevel): Boolean

}