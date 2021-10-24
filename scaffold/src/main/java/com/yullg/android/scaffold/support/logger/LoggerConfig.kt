package com.yullg.android.scaffold.support.logger

import com.yullg.android.scaffold.core.Constants

interface LoggerConfig {

    val consoleAppenderEnabled: Boolean

    val consoleAppenderLevel: LogLevel

    val fileAppenderEnabled: Boolean

    val fileAppenderLevel: LogLevel

    val logFileMaxLife: Int

    val uploader: LogUploader?

    fun findConsoleAppenderEnabled(name: String): Boolean

    fun findConsoleAppenderLevel(name: String): LogLevel

    fun findFileAppenderEnabled(name: String): Boolean

    fun findFileAppenderLevel(name: String): LogLevel

}

open class MutableLoggerConfig private constructor() : LoggerConfig {

    override var consoleAppenderEnabled: Boolean = true

    override var consoleAppenderLevel: LogLevel = LogLevel.TRACE

    override var fileAppenderEnabled: Boolean = true

    override var fileAppenderLevel: LogLevel = LogLevel.WARN

    override var logFileMaxLife: Int = 30

    override var uploader: LogUploader? = null

    private val loggerConfigOptionMap = HashMap<String, LoggerConfigOption>()

    init {
        logger(Constants.Logger.NAME_CRASH) {
            consoleAppenderEnabled = true
            consoleAppenderLevel = LogLevel.TRACE
            fileAppenderEnabled = true
            fileAppenderLevel = LogLevel.TRACE
        }
    }

    fun logger(name: String, block: LoggerConfigOption.() -> Unit) {
        val mutableLoggerConfigOption = loggerConfigOptionMap[name] ?: LoggerConfigOption()
        mutableLoggerConfigOption.block()
        loggerConfigOptionMap[name] = mutableLoggerConfigOption
    }

    override fun findConsoleAppenderEnabled(name: String): Boolean =
        if (Constants.Logger.NAME_CRASH != name) {
            consoleAppenderEnabled && (loggerConfigOptionMap[name]?.consoleAppenderEnabled ?: true)
        } else {
            loggerConfigOptionMap[name]?.consoleAppenderEnabled ?: consoleAppenderEnabled
        }

    override fun findConsoleAppenderLevel(name: String): LogLevel =
        loggerConfigOptionMap[name]?.consoleAppenderLevel ?: consoleAppenderLevel

    override fun findFileAppenderEnabled(name: String): Boolean =
        if (Constants.Logger.NAME_CRASH != name) {
            fileAppenderEnabled && (loggerConfigOptionMap[name]?.fileAppenderEnabled ?: true)
        } else {
            loggerConfigOptionMap[name]?.fileAppenderEnabled ?: fileAppenderEnabled
        }

    override fun findFileAppenderLevel(name: String): LogLevel =
        loggerConfigOptionMap[name]?.fileAppenderLevel ?: fileAppenderLevel

    internal companion object : MutableLoggerConfig()

}

data class LoggerConfigOption(
    var consoleAppenderEnabled: Boolean? = null,
    var consoleAppenderLevel: LogLevel? = null,
    var fileAppenderEnabled: Boolean? = null,
    var fileAppenderLevel: LogLevel? = null
)