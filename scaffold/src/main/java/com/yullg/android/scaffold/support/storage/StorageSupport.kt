package com.yullg.android.scaffold.support.storage

import androidx.annotation.WorkerThread
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream

/**
 * 提供文件存储功能的支持
 */
object StorageSupport {

    /**
     * 将文件[source]复制到[target]
     *
     * @throws FileAlreadyExistsException 如果目标文件已存在
     */
    @WorkerThread
    fun copyFrom(source: File, target: StorageFile) {
        source.copyTo(target.file)
    }

    /**
     * 将输入流[source]输出到[target]
     *
     * @throws FileAlreadyExistsException 如果目标文件已存在
     */
    @WorkerThread
    fun copyFrom(source: InputStream, target: StorageFile) {
        if (target.file.exists()) {
            throw FileAlreadyExistsException(
                file = target.file,
                reason = "The destination file already exists."
            )
        }
        target.file.parentFile?.mkdirs()
        target.file.outputStream().use {
            source.copyTo(it)
        }
    }

    /**
     * 将文件[source]复制到[target]
     *
     * @throws FileAlreadyExistsException 如果目标文件已存在
     */
    @WorkerThread
    fun copyTo(source: StorageFile, target: File) {
        source.file.copyTo(target)
    }

    /**
     * 将由[StorageFile]表示的文件复制到给定的输出流中
     *
     * @throws FileNotFoundException 如果源文件不存在
     */
    @WorkerThread
    fun copyTo(source: StorageFile, target: OutputStream) {
        source.file.inputStream().use {
            it.copyTo(target)
        }
    }

}