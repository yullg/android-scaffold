package com.yullg.android.scaffold.support.logger

import java.util.concurrent.TimeUnit

interface LoggerConfig {

    val consoleAppenderEnabled: Boolean

    val consoleAppenderLevel: LogLevel

    val fileAppenderEnabled: Boolean

    val fileAppenderLevel: LogLevel

    val logFileMaxLife: Int

    val uploadRepeatInterval: Pair<Long, TimeUnit>

    val uploadInitialDelay: Pair<Long, TimeUnit>

    val uploader: LogUploader?

    fun findConsoleAppenderEnabled(name: String): Boolean

    fun findConsoleAppenderLevel(name: String): LogLevel

    fun findFileAppenderEnabled(name: String): Boolean

    fun findFileAppenderLevel(name: String): LogLevel

    companion object : LoggerConfig by MutableLoggerConfig

}

open class MutableLoggerConfig private constructor() : LoggerConfig {

    override var consoleAppenderEnabled: Boolean = true

    override var consoleAppenderLevel: LogLevel = LogLevel.TRACE

    override var fileAppenderEnabled: Boolean = true

    override var fileAppenderLevel: LogLevel = LogLevel.WARN

    override var logFileMaxLife: Int = 30

    override var uploadRepeatInterval: Pair<Long, TimeUnit> = Pair(12, TimeUnit.HOURS)

    override var uploadInitialDelay: Pair<Long, TimeUnit> = Pair(0, TimeUnit.MINUTES)

    override var uploader: LogUploader? = null

    private val loggerConfigOptionMap = HashMap<String, LoggerConfigOption>()

    fun logger(name: String, block: LoggerConfigOption.() -> Unit) {
        val mutableLoggerConfigOption = loggerConfigOptionMap[name] ?: LoggerConfigOption()
        mutableLoggerConfigOption.block()
        loggerConfigOptionMap[name] = mutableLoggerConfigOption
    }

    override fun findConsoleAppenderEnabled(name: String): Boolean =
        consoleAppenderEnabled && (loggerConfigOptionMap[name]?.consoleAppenderEnabled ?: true)

    override fun findConsoleAppenderLevel(name: String): LogLevel =
        loggerConfigOptionMap[name]?.consoleAppenderLevel ?: consoleAppenderLevel

    override fun findFileAppenderEnabled(name: String): Boolean =
        fileAppenderEnabled && (loggerConfigOptionMap[name]?.fileAppenderEnabled ?: true)

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