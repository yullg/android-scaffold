package com.yullg.android.scaffold.support.logger

import com.yullg.android.scaffold.app.ScaffoldConfig
import com.yullg.android.scaffold.app.ScaffoldConstants

/**
 * 一个[ILogger]实现，提供了日志输出功能。
 *
 * 当前有两种日志输出类型：一种是通过[android.util.Log]输出，在Logcat中查看；
 * 另一种是写入缓存文件，文件存储在应用专属内部缓存目录下的`/yg/log`目录中。
 *
 * 当日志通过[android.util.Log]输出，[name]属性将作为`TAG`以标识日志来源；
 * 当日志写入缓存文件，[name]属性将作为文件名的前缀部分
 * （在这种情况下需要特别注意不能在[name]中包含本地文件系统不允许的字符，否则可能导致无法创建文件）。
 * 默认情况下日志采用异步方式输出，可以通过[synchronized]属性指定为同步方式。
 */
class Logger(override val name: String, private val synchronized: Boolean = false) : ILogger {

    /**
     * 记录日志
     */
    override fun log(log: Log) {
        LogManager.writeLog(log, synchronized)
    }

    /**
     * 判断日志级别是否启用。如果日志可以输出到Logcat或缓存文件，那么就返回true，否则返回false。
     */
    override fun isEnabled(logLevel: LogLevel): Boolean =
        (ScaffoldConfig.Logger.findConsoleAppenderEnabled(name)
                && ScaffoldConfig.Logger.findConsoleAppenderLevel(name) <= logLevel)
                || (ScaffoldConfig.Logger.findFileAppenderEnabled(name)
                && ScaffoldConfig.Logger.findFileAppenderLevel(name) <= logLevel)

    companion object : ILogger by Logger(ScaffoldConstants.Logger.NAME_DEFAULT)

}

/**
 * 定义日志输出相关的交互接口
 */
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