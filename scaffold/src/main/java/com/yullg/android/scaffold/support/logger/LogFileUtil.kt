package com.yullg.android.scaffold.support.logger

import com.yullg.android.scaffold.app.Scaffold
import com.yullg.android.scaffold.internal.ScaffoldLogger
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

internal object LogFileUtil {

    private val fileNameDateFormat = SimpleDateFormat("yyMMdd")
    private val fileNamePattern = Pattern.compile("^.*-(\\d{6}).log$")

    private val logDirectory: File by lazy {
        File(Scaffold.context.cacheDir, "/yg/log")
    }
    private val uploadLogDirectory: File by lazy {
        File(Scaffold.context.cacheDir, "/yg/log/upload")
    }

    fun writeLog(name: String, time: Date, content: String) {
        synchronized(this) {
            val logFile = File(logDirectory, "$name-${fileNameDateFormat.format(time)}.log")
            logFile.parentFile?.mkdirs()
            logFile.appendText(content)
        }
    }

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
                    "[LogFileUtil] Delete expired log file : thresholdDate = ${
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

    fun uploadEachLogFile(block: (File) -> Boolean) {
        val logContent = StringBuilder()
        val logEnabled = ScaffoldLogger.isDebugEnabled()
        if (logEnabled) {
            logContent.append("[LogFileUtil] Delete uploaded log file\n")
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
            logContent.append("[LogFileUtil] Mark the file to upload\n")
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
            logContent.append("[LogFileUtil] Delete uploaded log file\n")
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