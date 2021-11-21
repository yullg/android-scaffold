package com.yullg.android.scaffold.core

import com.yullg.android.scaffold.internal.ScaffoldLogger
import java.io.File
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 将Logcat日志转存到本地文件中
 *
 * 在[mount()]被调用后，将启动一个新线程持续从Logcat中读取日志数据并写入由[file()]指定的文件中。
 */
object LogcatDumper {

    const val PRIORITY_V: String = "V"
    const val PRIORITY_D: String = "D"
    const val PRIORITY_I: String = "I"
    const val PRIORITY_W: String = "W"
    const val PRIORITY_E: String = "E"
    const val PRIORITY_F: String = "F"
    const val PRIORITY_S: String = "S"

    private val mounted = AtomicBoolean(false)
    private var mPriority: String = PRIORITY_I
    private var mCharset: Charset = Charsets.UTF_8
    private var mFile: File? = null
    private var mDumpThread: Thread? = null

    fun priority(value: String): LogcatDumper {
        mPriority = when (value) {
            PRIORITY_V -> PRIORITY_V
            PRIORITY_D -> PRIORITY_D
            PRIORITY_I -> PRIORITY_I
            PRIORITY_W -> PRIORITY_W
            PRIORITY_E -> PRIORITY_E
            PRIORITY_F -> PRIORITY_F
            PRIORITY_S -> PRIORITY_S
            else -> throw IllegalArgumentException("$value is not a valid priority")
        }
        return this
    }

    fun charset(value: Charset): LogcatDumper {
        mCharset = value
        return this
    }

    fun file(value: File?): LogcatDumper {
        mFile = value
        return this
    }

    fun mount() {
        if (mounted.compareAndSet(false, true)) {
            mDumpThread = Thread(
                LogcatDumpRunnable(mPriority, mCharset, mFile),
                "scaffold_logcat_dump_thread"
            ).apply {
                start()
            }
            ScaffoldLogger.debug("[LogcatDumper] Mount succeeded")
        } else {
            ScaffoldLogger.warn("[LogcatDumper] Mount ignored")
        }
    }

    fun unmount() {
        if (mounted.compareAndSet(true, false)) {
            mDumpThread?.interrupt()
            mDumpThread = null
            ScaffoldLogger.debug("[LogcatDumper] Unmount succeeded")
        } else {
            ScaffoldLogger.warn("[LogcatDumper] Unmount ignored")
        }
    }

}

private class LogcatDumpRunnable(
    private val priority: String,
    private val charset: Charset,
    private val file: File?
) : Runnable {

    override fun run() {
        try {
            ScaffoldLogger.debug("[LogcatDumper] Dumping...")
            var process: Process? = null
            try {
                process = Runtime.getRuntime()
                    .exec("logcat --pid=${android.os.Process.myPid()} *:$priority")
                process.inputStream.bufferedReader(charset).use { reader ->
                    if (file != null) {
                        file.bufferedWriter(charset).use { writer ->
                            reader.lineSequence().forEach { line ->
                                writer.appendLine(line)
                            }
                        }
                    } else {
                        // 没有指定输出文件时仍然读取数据，这是为了让进程和logcat绑定
                        reader.lineSequence().forEach { _ -> }
                    }
                }
            } finally {
                process?.destroy()
            }
            // 实际不可能会正常结束，因为输入流是无限的
            ScaffoldLogger.warn("[LogcatDumper] Dump has stopped")
        } catch (e: Exception) {
            ScaffoldLogger.error("[LogcatDumper] Dump failed", e)
        }
    }

}