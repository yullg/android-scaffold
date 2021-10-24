package com.yullg.android.scaffold.support.logger

import android.os.Handler
import android.os.HandlerThread
import com.yullg.android.scaffold.app.ScaffoldConfig
import com.yullg.android.scaffold.core.Constants
import com.yullg.android.scaffold.helper.ExceptionHelper
import com.yullg.android.scaffold.internal.ScaffoldLogger
import java.text.SimpleDateFormat
import java.util.*

internal object Appender {

    private const val WHAT_DEL = 1
    private const val WHAT_WRT_CONS = 2
    private const val WHAT_WRT_FILE = 3
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ", Locale.US)
    private val handler: Handler

    init {
        val handlerThread = HandlerThread("scaffold_logger_thread")
        handlerThread.start()
        handler = Handler(handlerThread.looper) {
            if (WHAT_DEL == it.what) {
                doDeleteLogFiles(it.arg1)
            } else if (WHAT_WRT_CONS == it.what) {
                doWriteConsoleLog(it.obj as Log)
            } else if (WHAT_WRT_FILE == it.what) {
                doWriteFileLog(it.obj as Log)
            }
            true
        }
    }

    fun deleteLog(logFileMaxLife: Int) =
        handler.sendMessage(handler.obtainMessage(WHAT_DEL, logFileMaxLife, 0))

    fun writeLog(log: Log, synchronized: Boolean) {
        if (ScaffoldConfig.Logger.findConsoleAppenderEnabled(log.name)
            && ScaffoldConfig.Logger.findConsoleAppenderLevel(log.name) <= log.logLevel
        ) {
            if (synchronized) {
                doWriteConsoleLog(log)
            } else {
                handler.sendMessage(handler.obtainMessage(WHAT_WRT_CONS, log))
            }
        }
        if (ScaffoldConfig.Logger.findFileAppenderEnabled(log.name)
            && ScaffoldConfig.Logger.findFileAppenderLevel(log.name) <= log.logLevel
        ) {
            if (synchronized) {
                doWriteFileLog(log)
            } else {
                handler.sendMessage(handler.obtainMessage(WHAT_WRT_FILE, log))
            }
        }
    }

    private fun doDeleteLogFiles(logFileMaxLife: Int) {
        try {
            LogFileUtil.deleteLogFiles(logFileMaxLife)
        } catch (error: Exception) {
            ScaffoldLogger.error("[LogAppender] Failed to delete log file", error)
        }
    }

    private fun doWriteConsoleLog(log: Log) {
        try {
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
        } catch (error: Exception) {
            android.util.Log.e(
                Constants.Logger.TAG_SCAFFOLD,
                "[LogAppender] Failed to write console-log",
                error
            )
        }
    }

    private fun doWriteFileLog(log: Log) {
        try {
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
        } catch (error: Exception) {
            android.util.Log.e(
                Constants.Logger.TAG_SCAFFOLD,
                "[LogAppender] Failed to write file-log",
                error
            )
        }
    }

}