package com.yullg.android.scaffold.support.logger

import android.os.Handler
import android.os.HandlerThread
import com.yullg.android.scaffold.app.ScaffoldConfig
import com.yullg.android.scaffold.internal.ScaffoldLogcat

internal object LogAppender {

    private const val WHAT_WRT_CONS = 1
    private const val WHAT_WRT_FILE = 2
    private val handler: Handler

    init {
        val handlerThread = HandlerThread("scaffold_logger_thread")
        handlerThread.start()
        handler = Handler(handlerThread.looper) {
            if (WHAT_WRT_CONS == it.what) {
                doAppendConsoleLog(it.obj as Log)
            } else if (WHAT_WRT_FILE == it.what) {
                doAppendFileLog(it.obj as Log)
            }
            true
        }
    }

    fun doAppend(log: Log, synchronized: Boolean) {
        if (ScaffoldConfig.Logger.findConsoleAppenderEnabled(log.name)
            && ScaffoldConfig.Logger.findConsoleAppenderLevel(log.name) <= log.level
        ) {
            if (synchronized) {
                doAppendConsoleLog(log)
            } else {
                handler.sendMessage(handler.obtainMessage(WHAT_WRT_CONS, log))
            }
        }
        if (ScaffoldConfig.Logger.findFileAppenderEnabled(log.name)
            && ScaffoldConfig.Logger.findFileAppenderLevel(log.name) <= log.level
        ) {
            if (synchronized) {
                doAppendFileLog(log)
            } else {
                handler.sendMessage(handler.obtainMessage(WHAT_WRT_FILE, log))
            }
        }
    }

    private fun doAppendConsoleLog(log: Log) {
        try {
            when (log.level) {
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
            ScaffoldLogcat.e("[Logger] Failed to append console-log", error)
        }
    }

    private fun doAppendFileLog(log: Log) {
        try {
            LogFileManager.writeToFile(log)
        } catch (error: Exception) {
            ScaffoldLogcat.e("[Logger] Failed to append file-log", error)
        }
    }

}