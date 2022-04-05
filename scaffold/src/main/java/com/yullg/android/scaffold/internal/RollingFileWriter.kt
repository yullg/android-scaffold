package com.yullg.android.scaffold.internal

import androidx.annotation.FloatRange
import androidx.annotation.RestrictTo
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset

/**
 * 一个文件输出流，它查看当前写入的文件的大小，如果它增长到由[maxSize]指定的大小，
 * 那么将删除最先写入的内容来减小文件，删除的比例由[rollPercent]指定。
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class RollingFileWriter(
    val file: File,
    val maxSize: Long = -1,
    @FloatRange(from = 0.0, to = 1.0) val rollPercent: Float = 0.3F,
    val charset: Charset = Charsets.UTF_8
) : AutoCloseable {

    private var writer: BufferedWriter

    init {
        writer = FileOutputStream(file, true).writer(charset).buffered(DEFAULT_BUFFER_SIZE)
    }

    fun write(str: String) = synchronized(this) {
        writer.write(str)
        rollFile()
    }

    fun writeln(str: String) = synchronized(this) {
        writer.write(str)
        writer.newLine()
        rollFile()
    }

    override fun close() = synchronized(this) {
        writer.close()
    }

    private fun rollFile() {
        if (maxSize < 0) return // 仅小于0才表示禁止滚动，等于0将产生不可描述的现象
        if (file.length() <= maxSize) return
        try {
            writer.close()
            val tempFile = File("${file.absolutePath}.yg_temp")
            try {
                tempFile.outputStream().buffered().use { bos ->
                    file.inputStream().buffered().use { bis ->
                        bis.skip((file.length() * rollPercent).toLong())
                        bis.copyTo(bos)
                    }
                }
                file.delete()
                tempFile.renameTo(file)
            } finally {
                // 不管操作是否正常完成，都希望临时文件消失
                if (tempFile.exists()) {
                    tempFile.delete()
                }
            }
        } finally {
            // 不管操作是否正常完成，都希望输出流恢复，保证下次写入不受影响
            writer = FileOutputStream(file, true).writer(charset).buffered(DEFAULT_BUFFER_SIZE)
        }
    }

}