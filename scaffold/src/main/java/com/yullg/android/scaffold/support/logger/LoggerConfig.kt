package com.yullg.android.scaffold.support.logger

import androidx.work.NetworkType
import com.yullg.android.scaffold.app.ScaffoldConstants
import com.yullg.android.scaffold.helper.DateHelper

interface LoggerConfig {

    val consoleAppenderEnabled: Boolean

    val consoleAppenderLevel: LogLevel

    val fileAppenderEnabled: Boolean

    val fileAppenderLevel: LogLevel

    val logFileMaxLife: Int

    val uploader: LogUploader?

    val logUploadWorkerOption: LogUploadWorkerOption

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

    override var logUploadWorkerOption: LogUploadWorkerOption = OneTimeLogUploadWorkerOption(
        initialDelay = 5 * DateHelper.MILLIS_PER_MINUTE,
    )

    private val loggerConfigOptionMap = HashMap<String, LoggerConfigOption>()

    init {
        // 单独定义CRASH日志配置，让它不受全局配置影响
        logger(ScaffoldConstants.Logger.NAME_CRASH) {
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
        if (ScaffoldConstants.Logger.NAME_CRASH != name) {
            consoleAppenderEnabled && (loggerConfigOptionMap[name]?.consoleAppenderEnabled ?: true)
        } else {
            loggerConfigOptionMap[name]?.consoleAppenderEnabled ?: consoleAppenderEnabled
        }

    override fun findConsoleAppenderLevel(name: String): LogLevel =
        loggerConfigOptionMap[name]?.consoleAppenderLevel ?: consoleAppenderLevel

    override fun findFileAppenderEnabled(name: String): Boolean =
        if (ScaffoldConstants.Logger.NAME_CRASH != name) {
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

sealed interface LogUploadWorkerOption

/**
 * 定义周期性日志上传工作的配置选项
 */
data class PeriodicLogUploadWorkerOption(
    val repeatInterval: Long,
    val initialDelay: Long,
    val requiredNetworkType: NetworkType = NetworkType.CONNECTED
) : LogUploadWorkerOption

/**
 * 定义一次性日志上传工作的配置选项
 */
data class OneTimeLogUploadWorkerOption(
    val initialDelay: Long,
    val requiredNetworkType: NetworkType = NetworkType.CONNECTED
) : LogUploadWorkerOption