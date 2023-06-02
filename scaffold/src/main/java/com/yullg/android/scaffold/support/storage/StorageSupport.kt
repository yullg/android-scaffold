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
     * 将文件[source]复制到[destination]
     *
     * 如果在通往[destination]的路径上缺少一些目录，那么将创建它们。如果[destination]已经存在，则此方法将失败。
     *
     * @throws FileAlreadyExistsException 如果目标文件已存在
     */
    @WorkerThread
    fun copyFrom(source: File, destination: StorageFile) {
        source.copyTo(destination.file)
    }

    /**
     * 将输入流[source]输出到[destination]
     *
     * 如果在通往[destination]的路径上缺少一些目录，那么将创建它们。如果[destination]已经存在，则此方法将失败。
     *
     * @throws FileAlreadyExistsException 如果目标文件已存在
     */
    @WorkerThread
    fun copyFrom(source: InputStream, destination: StorageFile) {
        if (destination.file.exists()) {
            throw FileAlreadyExistsException(
                file = destination.file,
                reason = "The destination file already exists."
            )
        }
        destination.file.parentFile?.mkdirs()
        destination.file.outputStream().use {
            source.copyTo(it)
        }
    }

    /**
     * 将文件[source]复制到[destination]
     *
     * 如果在通往[destination]的路径上缺少一些目录，那么将创建它们。如果[destination]已经存在，则此方法将失败。
     *
     * @throws FileAlreadyExistsException 如果目标文件已存在
     */
    @WorkerThread
    fun copyTo(source: StorageFile, destination: File) {
        source.file.copyTo(destination)
    }

    /**
     * 将由[StorageFile]表示的文件复制到给定的输出流中
     *
     * @throws FileNotFoundException 如果源文件不存在
     */
    @WorkerThread
    fun copyTo(source: StorageFile, destination: OutputStream) {
        source.file.inputStream().use {
            it.copyTo(destination)
        }
    }

}