package com.yullg.android.scaffold.support.logger

import com.yullg.android.scaffold.app.Scaffold
import com.yullg.android.scaffold.app.ScaffoldConstants
import com.yullg.android.scaffold.internal.ScaffoldLogger
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

internal object LogFileUtil {

    private val fileNameDateFormat = SimpleDateFormat("yyMMdd", Locale.US)
    private val fileNamePattern = Pattern.compile("^.*-(\\d{6}).log$")

    /**
     * 日志的输出目录
     */
    private val logDirectory: File by lazy {
        File(Scaffold.context.cacheDir, ScaffoldConstants.Logger.DIR_LOG)
    }

    /**
     * 日志在上传前的暂存目录，所有日志文件在上传前都会先从输出目录移动到此目录下。
     */
    private val uploadLogDirectory: File by lazy {
        File(Scaffold.context.cacheDir, ScaffoldConstants.Logger.DIR_LOG_UPLOAD)
    }

    /**
     * 写入日志
     */
    fun writeLog(name: String, time: Date, content: String) {
        synchronized(this) {
            val logFile = File(logDirectory, "$name-${fileNameDateFormat.format(time)}.log")
            logFile.parentFile?.mkdirs()
            logFile.appendText(content)
        }
    }

    /**
     * 删除日志目录下每个[logFileMaxLife]天之前的日志文件
     */
    fun deleteLogFiles(logFileMaxLife: Int) {
        synchronized(this) {
            val logContent = StringBuilder()
            val logEnabled = ScaffoldLogger.isInfoEnabled()
            val thresholdDate = Calendar.getInstance().let {
                it.add(Calendar.DAY_OF_YEAR, -logFileMaxLife)
                it.time
            }
            if (logEnabled) {
                logContent.append(
                    "[Logger] Delete the expired log file: thresholdDate = ${
                        fileNameDateFormat.format(thresholdDate)
                    }\n"
                )
            }
            eachLogFile { file ->
                val matcher = fileNamePattern.matcher(file.name)
                if (matcher.matches()) {
                    if (thresholdDate.after(fileNameDateFormat.parse(matcher.group(1)!!))) {
                        file.delete().let {
                            if (logEnabled) {
                                logContent.append("$file - > $it\n")
                            }
                        }
                    }
                }
            }
            if (logEnabled) {
                logContent.append("------ over ------")
                ScaffoldLogger.info(logContent.toString())
            }
        }
    }

    /**
     * 首先遍历上传目录下的每个日志文件，调用[block]去执行上传操作，如果上传成功（[block]返回`true`）那么就删除这个日志文件；
     * 然后获取同步锁，阻塞日志文件写入和删除操作，移动日志目录下的每个日志文件到上传目录，如果上传目录下已经有同名文件，那么忽略等待下次移动。
     * 最后释放同步锁后再执行一次上传操作。
     */
    fun uploadEachLogFile(block: (File) -> Boolean) {
        val logContent = StringBuilder()
        val logEnabled = ScaffoldLogger.isDebugEnabled()
        if (logEnabled) {
            logContent.append("[Logger - upload] Delete the uploaded log file\n")
        }
        eachUploadLogFile { file ->
            if (block(file)) {
                file.delete().let {
                    if (logEnabled) {
                        logContent.append("$file -> $it\n")
                    }
                }
            }
        }
        if (logEnabled) {
            logContent.append("------ over ------")
            ScaffoldLogger.debug(logContent.toString())
        }
        logContent.clear()
        if (logEnabled) {
            logContent.append("[Logger - upload] Mark the file to upload\n")
        }
        synchronized(this) {
            eachLogFile { file ->
                val newFile = File(uploadLogDirectory, file.name)
                if (!newFile.exists()) {
                    newFile.parentFile?.mkdirs()
                    file.renameTo(newFile).let {
                        if (logEnabled) {
                            logContent.append("$file -> $newFile -> $it\n")
                        }
                    }
                } else {
                    if (logEnabled) {
                        logContent.append("$file -> $newFile -> Skip : File exists\n")
                    }
                }
            }
        }
        if (logEnabled) {
            logContent.append("------ over ------")
            ScaffoldLogger.debug(logContent.toString())
        }
        logContent.clear()
        if (logEnabled) {
            logContent.append("[Logger - upload] Delete the uploaded log file\n")
        }
        eachUploadLogFile { file ->
            if (block(file)) {
                file.delete().let {
                    if (logEnabled) {
                        logContent.append("$file -> $it\n")
                    }
                }
            }
        }
        if (logEnabled) {
            logContent.append("------ over ------")
            ScaffoldLogger.debug(logContent.toString())
        }
    }

    /**
     * 遍历日志目录下的每一个日志文件，忽略所有无关的文件
     */
    private fun eachLogFile(block: (File) -> Unit) {
        if (logDirectory.exists() && logDirectory.isDirectory) {
            logDirectory.listFiles()?.let { files ->
                for (file in files) {
                    if (file.isFile && fileNamePattern.matcher(file.name).matches()) {
                        block(file)
                    }
                }
            }
        }
    }

    /**
     * 遍历日志上传目录下的每一个日志文件，忽略所有无关的文件
     */
    private fun eachUploadLogFile(block: (File) -> Unit) {
        if (uploadLogDirectory.exists() && uploadLogDirectory.isDirectory) {
            uploadLogDirectory.listFiles()?.let { files ->
                for (file in files) {
                    if (file.isFile && fileNamePattern.matcher(file.name).matches()) {
                        block(file)
                    }
                }
            }
        }
    }

}