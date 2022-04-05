package com.yullg.android.scaffold.support.logger

import com.yullg.android.scaffold.app.ScaffoldConfig
import com.yullg.android.scaffold.app.ScaffoldConstants
import com.yullg.android.scaffold.internal.ScaffoldLogger
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 将Logcat日志转存到本地日志文件中
 */
object LogcatDumper {

    private val mounted = AtomicBoolean(false)

    private var mLogCharset: Charset = Charsets.UTF_8

    private var mDumpThread: LogcatDumpThread? = null

    /**
     * 设置日志字符编码（默认为[Charsets.UTF_8]）
     */
    fun logCharset(value: Charset): LogcatDumper {
        mLogCharset = value
        return this
    }

    fun mount() {
        if (mounted.compareAndSet(false, true)) {
            mDumpThread = LogcatDumpThread().apply {
                start()
            }
            ScaffoldLogger.info("[LogcatDumper] Mount succeeded")
        } else {
            ScaffoldLogger.warn("[LogcatDumper] Mount ignored")
        }
    }

    fun unmount() {
        if (mounted.compareAndSet(true, false)) {
            mDumpThread?.kill()
            mDumpThread = null
            ScaffoldLogger.info("[LogcatDumper] Unmount succeeded")
        } else {
            ScaffoldLogger.warn("[LogcatDumper] Unmount ignored")
        }
    }

    private class LogcatDumpThread : Thread("scaffold_logcat_dump_thread") {

        var process: Process? = null

        override fun run() {
            try {
                ScaffoldLogger.info("[LogcatDumper] Dumping...")
                try {
                    val logPriority =
                        ScaffoldConfig.Logger.findFileAppenderLevel(ScaffoldConstants.Logger.NAME_LOGCAT_DUMPER)
                            .let {
                                when (it) {
                                    LogLevel.TRACE -> "V"
                                    LogLevel.DEBUG -> "D"
                                    LogLevel.INFO -> "I"
                                    LogLevel.WARN -> "W"
                                    LogLevel.ERROR -> "E"
                                    LogLevel.FATAL -> "F"
                                }
                            }
                    val command = "logcat --pid=${android.os.Process.myPid()} *:$logPriority"
                    ScaffoldLogger.info("[LogcatDumper] Ready to run the command: $command")
                    process = Runtime.getRuntime().exec(command)
                    process?.inputStream?.bufferedReader(mLogCharset)?.use { reader ->
                        if (ScaffoldConfig.Logger.findFileAppenderEnabled(ScaffoldConstants.Logger.NAME_LOGCAT_DUMPER)) {
                            // 执行转存的整个流程中，自身不能再向Logcat发送任何数据，否则可能陷入死循环。
                            reader.lineSequence().forEach { line ->
                                try {
                                    LogFileManager.writelnToFile(
                                        ScaffoldConstants.Logger.NAME_LOGCAT_DUMPER,
                                        Date(),
                                        line
                                    )
                                } catch (e: Exception) {
                                    // nothing
                                }
                            }
                        } else {
                            // 文件输出未启用时仍然读取数据，这是为了让进程和logcat绑定
                            ScaffoldLogger.warn("[LogcatDumper] File appender is not enabled")
                            reader.lineSequence().forEach { _ -> }
                        }
                    }
                } finally {
                    process?.destroy()
                    process = null
                }
                // 实际不可能会正常结束，因为输入流是无限的
                ScaffoldLogger.warn("[LogcatDumper] Dump has stopped")
            } catch (e: Exception) {
                ScaffoldLogger.error("[LogcatDumper] Dump failed", e)
            }
        }

        fun kill() {
            process?.destroy()
            process = null
            interrupt()
        }

    }

}