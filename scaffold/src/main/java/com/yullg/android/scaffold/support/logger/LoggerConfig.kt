package com.yullg.android.scaffold.support.logger

import androidx.work.NetworkType
import com.yullg.android.scaffold.app.ScaffoldConstants
import com.yullg.android.scaffold.helper.DateHelper

/**
 * 日志配置（只读版本）
 */
interface LoggerConfig {

    /**
     * 日志控制台输出是否已启用
     */
    val consoleAppenderEnabled: Boolean

    /**
     * 日志控制台输出级别
     */
    val consoleAppenderLevel: LogLevel

    /**
     * 日志文件输出是否已启用
     */
    val fileAppenderEnabled: Boolean

    /**
     * 日志文件输出级别
     */
    val fileAppenderLevel: LogLevel

    /**
     * 日志文件最大大小（单位：字节），如果超过此大小将删除部分最先写入的内容来缩小文件，指定小于0的值表示文件大小不受限。
     */
    val logFileMaxSize: Long

    /**
     * 日志文件保留的最长时间（单位：天），应用将在每次启动时删除超过此时间限制的日志文件。
     */
    val logFileMaxLife: Int

    /**
     * 日志上传程序，设置为NULL将不会上传日志。
     */
    val uploader: LogFileUploader?

    /**
     * 日志上传程序的配置选项
     */
    val logUploadWorkerOption: LogUploadWorkerOption

    fun findConsoleAppenderEnabled(name: String): Boolean

    fun findConsoleAppenderLevel(name: String): LogLevel

    fun findFileAppenderEnabled(name: String): Boolean

    fun findFileAppenderLevel(name: String): LogLevel

    fun findLogFileMaxSize(name: String): Long

    fun findLogFileMaxLife(name: String): Int

}

/**
 * 日志配置（读写版本）
 */
open class MutableLoggerConfig private constructor() : LoggerConfig {

    override var consoleAppenderEnabled: Boolean = true

    override var consoleAppenderLevel: LogLevel = LogLevel.TRACE

    override var fileAppenderEnabled: Boolean = true

    override var fileAppenderLevel: LogLevel = LogLevel.WARN

    override val logFileMaxSize: Long = -1

    override var logFileMaxLife: Int = 30

    override var uploader: LogFileUploader? = null

    override var logUploadWorkerOption: LogUploadWorkerOption = OneTimeLogUploadWorkerOption(
        initialDelay = 5 * DateHelper.MILLIS_PER_MINUTE,
    )

    private val loggerConfigOptionMap = HashMap<String, LoggerConfigOption>()

    init {
        // 单独定义日志配置，让它不受全局配置影响
        logger(ScaffoldConstants.Logger.NAME_CRASH) {
            consoleAppenderEnabled = true
            consoleAppenderLevel = LogLevel.TRACE
            fileAppenderEnabled = true
            fileAppenderLevel = LogLevel.TRACE
        }
        logger(ScaffoldConstants.Logger.NAME_LOGCAT_DUMPER) {
            consoleAppenderEnabled = false
            fileAppenderEnabled = false
            logFileMaxSize = 10485760
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

    override fun findLogFileMaxSize(name: String): Long =
        loggerConfigOptionMap[name]?.logFileMaxSize ?: logFileMaxSize

    override fun findLogFileMaxLife(name: String): Int =
        loggerConfigOptionMap[name]?.logFileMaxLife ?: logFileMaxLife

    internal companion object : MutableLoggerConfig()

}

data class LoggerConfigOption(
    var consoleAppenderEnabled: Boolean? = null,
    var consoleAppenderLevel: LogLevel? = null,
    var fileAppenderEnabled: Boolean? = null,
    var fileAppenderLevel: LogLevel? = null,
    var logFileMaxSize: Long? = null,
    var logFileMaxLife: Int? = null
)

/**
 * 日志上传工作的配置选项
 */
sealed interface LogUploadWorkerOption

/**
 * 周期性日志上传工作的配置选项
 */
data class PeriodicLogUploadWorkerOption(
    val repeatInterval: Long,
    val initialDelay: Long,
    val requiredNetworkType: NetworkType = NetworkType.CONNECTED
) : LogUploadWorkerOption

/**
 * 一次性日志上传工作的配置选项
 */
data class OneTimeLogUploadWorkerOption(
    val initialDelay: Long,
    val requiredNetworkType: NetworkType = NetworkType.CONNECTED
) : LogUploadWorkerOption