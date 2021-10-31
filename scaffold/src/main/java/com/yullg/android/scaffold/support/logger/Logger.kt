package com.yullg.android.scaffold.support.logger

import com.yullg.android.scaffold.app.ScaffoldConfig
import com.yullg.android.scaffold.app.ScaffoldConstants

class Logger(override val name: String, private val synchronized: Boolean = false) : ILogger {

    override fun log(log: Log) {
        Appender.writeLog(log, synchronized)
    }

    override fun isEnabled(logLevel: LogLevel): Boolean =
        (ScaffoldConfig.Logger.findConsoleAppenderEnabled(name)
                && ScaffoldConfig.Logger.findConsoleAppenderLevel(name) <= logLevel)
                || (ScaffoldConfig.Logger.findFileAppenderEnabled(name)
                && ScaffoldConfig.Logger.findFileAppenderLevel(name) <= logLevel)

    companion object : ILogger by Logger(ScaffoldConstants.Logger.NAME_DEFAULT)

}

interface ILogger {

    val name: String

    fun trace(message: Any?) = trace(message, null)

    fun trace(message: Any?, error: Throwable?) = log(Log(name, LogLevel.TRACE, message, error))

    fun debug(message: Any?) = debug(message, null)

    fun debug(message: Any?, error: Throwable?) = log(Log(name, LogLevel.DEBUG, message, error))

    fun info(message: Any?) = info(message, null)

    fun info(message: Any?, error: Throwable?) = log(Log(name, LogLevel.INFO, message, error))

    fun warn(message: Any?) = warn(message, null)

    fun warn(message: Any?, error: Throwable?) = log(Log(name, LogLevel.WARN, message, error))

    fun error(message: Any?) = error(message, null)

    fun error(message: Any?, error: Throwable?) = log(Log(name, LogLevel.ERROR, message, error))

    fun fatal(message: Any?) = fatal(message, null)

    fun fatal(message: Any?, error: Throwable?) = log(Log(name, LogLevel.FATAL, message, error))

    fun isTraceEnabled() = isEnabled(LogLevel.TRACE)

    fun isDebugEnabled() = isEnabled(LogLevel.DEBUG)

    fun isInfoEnabled() = isEnabled(LogLevel.INFO)

    fun isWarnEnabled() = isEnabled(LogLevel.WARN)

    fun isErrorEnabled() = isEnabled(LogLevel.ERROR)

    fun isFatalEnabled() = isEnabled(LogLevel.FATAL)

    fun log(log: Log)

    fun isEnabled(logLevel: LogLevel): Boolean

}