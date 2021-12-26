package com.yullg.android.scaffold.support.storage

import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * 提供文件存储功能的支持
 */
object StorageSupport {

    /**
     * 判断给定[file]是否属于由[StorageDirectory]表示的存储目录
     */
    fun belong(file: File): Boolean =
        file.startsWith(StorageDirectory.FILES.file)
                || file.startsWith(StorageDirectory.CACHE.file)
                || file.startsWith(StorageDirectory.EXTERNAL_FILES.file)
                || file.startsWith(StorageDirectory.EXTERNAL_CACHE.file)

    /**
     * 获取给定[file]的存储目录和相对路径
     *
     * @throws IllegalArgumentException 如果[file]不属于任何[StorageDirectory]
     */
    fun split(file: File): Pair<StorageDirectory, String> =
        if (file.startsWith(StorageDirectory.FILES.file)) {
            Pair(StorageDirectory.FILES, file.toRelativeString(StorageDirectory.FILES.file))
        } else if (file.startsWith(StorageDirectory.CACHE.file)) {
            Pair(StorageDirectory.CACHE, file.toRelativeString(StorageDirectory.CACHE.file))
        } else if (file.startsWith(StorageDirectory.EXTERNAL_FILES.file)) {
            Pair(
                StorageDirectory.EXTERNAL_FILES,
                file.toRelativeString(StorageDirectory.EXTERNAL_FILES.file)
            )
        } else if (file.startsWith(StorageDirectory.EXTERNAL_CACHE.file)) {
            Pair(
                StorageDirectory.EXTERNAL_CACHE,
                file.toRelativeString(StorageDirectory.EXTERNAL_CACHE.file)
            )
        } else throw IllegalArgumentException("File does not belong to any StorageDirectory: $file.")

    /**
     * 根据给定的存储目录和相对路径创建[File]实例
     */
    fun newFile(storageDirectory: StorageDirectory, relativePath: String): File =
        File(storageDirectory.file, relativePath)

    /**
     * 根据给定的存储目录和相对路径创建[InputStream]实例
     */
    fun newInputStream(storageDirectory: StorageDirectory, relativePath: String): InputStream =
        FileInputStream(newFile(storageDirectory, relativePath))

    /**
     * 根据给定的存储目录和相对路径创建[Reader]实例
     */
    fun newReader(
        storageDirectory: StorageDirectory,
        relativePath: String,
        charset: Charset = StandardCharsets.UTF_8
    ): Reader = InputStreamReader(newInputStream(storageDirectory, relativePath), charset)

    /**
     * 根据给定的存储目录和相对路径创建[OutputStream]实例
     */
    fun newOutputStream(
        storageDirectory: StorageDirectory,
        relativePath: String,
        append: Boolean = false
    ): OutputStream {
        val file = newFile(storageDirectory, relativePath)
        file.parentFile?.mkdirs()
        return FileOutputStream(file, append)
    }

    /**
     * 根据给定的存储目录和相对路径创建[Writer]实例
     */
    fun newWriter(
        storageDirectory: StorageDirectory,
        relativePath: String,
        append: Boolean = false,
        charset: Charset = StandardCharsets.UTF_8
    ): Writer = OutputStreamWriter(newOutputStream(storageDirectory, relativePath, append), charset)

    /**
     * 将给定文件复制到由[StorageDirectory]表示的目录中，返回目标文件。
     *
     * @throws FileAlreadyExistsException 如果目标文件已经存在
     */
    fun copyFrom(file: File, storageDirectory: StorageDirectory, relativePath: String): File {
        val targetFile = newFile(storageDirectory, relativePath)
        file.copyTo(targetFile)
        return targetFile
    }

    /**
     * 将给定输入流输出到由[StorageDirectory]表示的目录中，返回目标文件。
     *
     * @throws FileAlreadyExistsException 如果目标文件已经存在
     */
    fun copyFrom(
        inputStream: InputStream,
        storageDirectory: StorageDirectory,
        relativePath: String
    ): File {
        val targetFile = newFile(storageDirectory, relativePath)
        if (targetFile.exists()) {
            throw FileAlreadyExistsException(
                file = targetFile,
                reason = "The destination file already exists."
            )
        }
        targetFile.parentFile?.mkdirs()
        targetFile.outputStream().use {
            inputStream.copyTo(it)
        }
        return targetFile
    }

    /**
     * 将由[StorageDirectory]表示的目录中的文件复制到给定的文件中，返回源文件。
     *
     * @throws FileAlreadyExistsException 如果目标文件已经存在
     */
    fun copyTo(storageDirectory: StorageDirectory, relativePath: String, file: File): File {
        val sourceFile = newFile(storageDirectory, relativePath)
        sourceFile.copyTo(file)
        return sourceFile
    }

    /**
     * 将由[StorageDirectory]表示的目录中的文件复制到给定的输出流中，返回源文件。
     */
    fun copyTo(
        storageDirectory: StorageDirectory,
        relativePath: String,
        outputStream: OutputStream
    ): File {
        val sourceFile = newFile(storageDirectory, relativePath)
        sourceFile.inputStream().use {
            it.copyTo(outputStream)
        }
        return sourceFile
    }

}