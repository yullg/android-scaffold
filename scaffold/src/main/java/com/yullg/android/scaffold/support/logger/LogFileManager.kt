package com.yullg.android.scaffold.support.logger

import com.yullg.android.scaffold.app.Scaffold
import com.yullg.android.scaffold.app.ScaffoldConfig
import com.yullg.android.scaffold.app.ScaffoldConstants
import com.yullg.android.scaffold.helper.ExceptionHelper
import com.yullg.android.scaffold.internal.RollingFileWriter
import com.yullg.android.scaffold.internal.ScaffoldLogger
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * 管理本地日志文件
 */
internal object LogFileManager {

    /**
     * 日志文件名中时间的格式化器
     */
    private val logFileNameDateFormat = SimpleDateFormat("yyMMdd", Locale.US)

    /**
     * 日志文件内容中时间的格式化器
     */
    private val logTimeDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ", Locale.US)

    /**
     * 用于解析日志文件名的正则表达式
     */
    private val logFileNamePattern = Pattern.compile("^(.+)-(\\d{6}).log$")

    /**
     * 日志文件目录
     */
    private val logFileDirectory: File by lazy {
        File(Scaffold.context.cacheDir, ScaffoldConstants.Logger.DIR_LOG)
    }

    /**
     * 待上传的日志文件目录，所有日志文件在上传前都会先从[logFileDirectory]目录移动到此目录下。
     */
    private val logFileUploadDirectory: File by lazy {
        File(Scaffold.context.cacheDir, ScaffoldConstants.Logger.DIR_LOG_UPLOAD)
    }

    /**
     * 已打开的日志输出流，其中Key是日志的名称([Log.name])，Value是输出流。
     */
    private val logWriterMap = mutableMapOf<String, RollingFileWriter>()

    /**
     * 写日志到文件
     */
    fun writeToFile(log: Log) = synchronized(this) {
        getOrCreateLogWriter(log.name, log.time).write(logToString(log))
    }

    /**
     * 写日志到文件
     */
    fun writelnToFile(logName: String, logTime: Date, str: String) = synchronized(this) {
        getOrCreateLogWriter(logName, logTime).writeln(str)
    }

    /**
     * 删除已过期的日志文件
     */
    fun deleteExpiredLogFile() = synchronized(this) {
        eachLogFile { logFile ->
            val expiredDate = Calendar.getInstance().let {
                val logFileMaxLife = ScaffoldConfig.Logger.findLogFileMaxLife(logFile.name)
                it.add(Calendar.DAY_OF_YEAR, -logFileMaxLife)
                it.time
            }
            if (expiredDate.after(logFile.time)) {
                // 删除过期的日志文件前先移除并且关闭可能已经打开的输出流
                if (logFile.file == logWriterMap[logFile.name]?.file) {
                    logWriterMap.remove(logFile.name)?.close()
                }
                // 删除过期的日志文件
                logFile.file.delete().let {
                    ScaffoldLogger.info(
                        "[Logger] Delete the expired log file: file = ${logFile.file}, expiredDate = ${
                            logFileNameDateFormat.format(expiredDate)
                        }, result = $it"
                    )
                }
            }
        }
    }

    /**
     * 首先遍历上传目录下的每个日志文件，调用[block]去执行上传操作，如果上传成功（[block]返回`true`）那么就删除这个日志文件；
     * 然后移动日志目录下的每个日志文件到上传目录，如果上传目录下已经有同名文件（可能上一次未上传成功），那么忽略等待下次再移动；
     * 最后再执行一次上传操作。
     */
    fun uploadEachLogFile(block: (LogFile) -> Boolean) {
        eachUploadLogFile { logFile ->
            if (block(logFile)) {
                logFile.file.delete().let {
                    ScaffoldLogger.info("[Logger] Delete the uploaded log file: file = ${logFile.file}, result = $it")
                }
            }
        }
        synchronized(this) {
            eachLogFile { logFile ->
                val uploadFile = File(logFileUploadDirectory, logFile.name)
                if (uploadFile.exists()) {
                    ScaffoldLogger.info("[Logger] Mark the file to upload: file = ${logFile.file}, result = conflict")
                } else {
                    // 移动日志文件前先移除并且关闭可能已经打开的输出流
                    if (logFile.file == logWriterMap[logFile.name]?.file) {
                        logWriterMap.remove(logFile.name)?.close()
                    }
                    // 移动日志文件
                    uploadFile.parentFile?.mkdirs()
                    logFile.file.renameTo(uploadFile).let {
                        ScaffoldLogger.info("[Logger] Mark the file to upload: file = ${logFile.file}, result = $it")
                    }
                }
            }
        }
        eachUploadLogFile { logFile ->
            if (block(logFile)) {
                logFile.file.delete().let {
                    ScaffoldLogger.info("[Logger] Delete the uploaded log file: file = ${logFile.file}, result = $it")
                }
            }
        }
    }

    /**
     * 获取日志相应的输出流，如果已有打开的输出流，就直接使用，否则就新创建一个并且保存下来。
     */
    private fun getOrCreateLogWriter(logName: String, logTime: Date): RollingFileWriter {
        val logFile =
            File(logFileDirectory, "$logName-${logFileNameDateFormat.format(logTime)}.log")
        val currLogWriter = logWriterMap[logName]
        if (currLogWriter != null) {
            if (logFile == currLogWriter.file) {
                return currLogWriter
            } else {
                // 已有打开的同名输出流，但是指向的文件不一致，移除并关闭这个流
                logWriterMap.remove(logName)?.close()
            }
        }
        // 打开一个新的输出流
        logFile.parentFile?.mkdirs()
        val logWriter =
            RollingFileWriter(logFile, ScaffoldConfig.Logger.findLogFileMaxSize(logName))
        logWriterMap[logName] = logWriter
        return logWriter
    }

    /**
     * 转换[Log]实例到文本
     */
    private fun logToString(log: Log): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(
            "${log.processId}\t${log.threadId}\t${log.threadName}\t${log.name}\t${log.level.name}\t${
                logTimeDateFormat.format(log.time)
            }\t${log.message?.toString() ?: "---"}\t${log.error?.message ?: "---"}\n"
        )
        if (log.error != null) {
            stringBuilder.append(ExceptionHelper.getStackTraceString(log.error))
        }
        return stringBuilder.toString()
    }

    /**
     * 遍历日志目录下的每一个日志文件
     */
    private fun eachLogFile(block: (LogFile) -> Unit) {
        if (logFileDirectory.exists() && logFileDirectory.isDirectory) {
            logFileDirectory.listFiles()?.let { files ->
                for (file in files) {
                    if (file.isFile) {
                        val matcher = logFileNamePattern.matcher(file.name)
                        if (matcher.matches()) {
                            block(
                                LogFile(
                                    name = matcher.group(1)!!,
                                    time = logFileNameDateFormat.parse(matcher.group(2)!!)!!,
                                    file = file
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 遍历待上传的日志目录下的每一个日志文件
     */
    private fun eachUploadLogFile(block: (LogFile) -> Unit) {
        if (logFileUploadDirectory.exists() && logFileUploadDirectory.isDirectory) {
            logFileUploadDirectory.listFiles()?.let { files ->
                for (file in files) {
                    if (file.isFile) {
                        val matcher = logFileNamePattern.matcher(file.name)
                        if (matcher.matches()) {
                            block(
                                LogFile(
                                    name = matcher.group(1)!!,
                                    time = logFileNameDateFormat.parse(matcher.group(2)!!)!!,
                                    file = file
                                )
                            )
                        }
                    }
                }
            }
        }
    }

}