package com.yullg.android.scaffold.support.logger

import android.os.Handler
import android.os.HandlerThread
import com.yullg.android.scaffold.app.ScaffoldConfig
import com.yullg.android.scaffold.core.Constants
import com.yullg.android.scaffold.helper.ExceptionHelper
import com.yullg.android.scaffold.internal.ScaffoldLogger
import java.text.SimpleDateFormat

internal object Appender {

    private const val WHAT_LOG = 1
    private const val WHAT_DEL = 2
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ")
    private val handler: Handler

    init {
        val handlerThread = HandlerThread("scaffold_logger_thread")
        handlerThread.start()
        handler = Handler(handlerThread.looper) {
            if (WHAT_LOG == it.what) {
                try {
                    doWriteLog(it.obj as Log)
                } catch (error: Exception) {
                    android.util.Log.e(
                        Constants.Logger.TAG_SCAFFOLD,
                        "[LogAppender] Failed to write log",
                        error
                    )
                }
            } else if (WHAT_DEL == it.what) {
                try {
                    LogFileUtil.deleteLogFiles(it.arg1)
                } catch (error: Exception) {
                    ScaffoldLogger.error("[LogAppender] Failed to delete log file", error)
                }
            }
            true
        }
    }

    fun deleteLog(logFileMaxLife: Int) =
        handler.sendMessage(handler.obtainMessage(WHAT_DEL, logFileMaxLife, 0))

    fun writeLog(log: Log) =
        handler.sendMessage(handler.obtainMessage(WHAT_LOG, log))

    private fun doWriteLog(log: Log) {
        if (ScaffoldConfig.Logger.findConsoleAppenderEnabled(log.name)
            && ScaffoldConfig.Logger.findConsoleAppenderLevel(log.name) <= log.logLevel
        ) {
            doWriteConsoleLog(log)
        }
        if (ScaffoldConfig.Logger.findFileAppenderEnabled(log.name)
            && ScaffoldConfig.Logger.findFileAppenderLevel(log.name) <= log.logLevel
        ) {
            doWriteFileLog(log)
        }
    }

    private fun doWriteConsoleLog(log: Log) {
        when (log.logLevel) {
            LogLevel.TRACE -> android.util.Log.v(log.name, log.message?.toString(), log.error)
            LogLevel.DEBUG -> android.util.Log.d(log.name, log.message?.toString(), log.error)
            LogLevel.INFO -> android.util.Log.i(log.name, log.message?.toString(), log.error)
            LogLevel.WARN -> android.util.Log.w(log.name, log.message?.toString(), log.error)
            LogLevel.ERROR, LogLevel.FATAL -> android.util.Log.e(
                log.name,
                log.message?.toString(),
                log.error
            )
        }
    }

    private fun doWriteFileLog(log: Log) {
        val stringifyLog = StringBuilder()
        stringifyLog.append(
            "${log.processId}\t${log.threadId}\t${log.threadName}\t${log.name}\t${log.logLevel.name}\t${
                dateFormat.format(log.time)
            }\t${log.message?.toString() ?: "---"}\t${log.error?.message ?: "---"}\n"
        )
        if (log.error != null) {
            stringifyLog.append(ExceptionHelper.getStackTraceString(log.error))
        }
        LogFileUtil.writeLog(log.name, log.time, stringifyLog.toString())
    }

}