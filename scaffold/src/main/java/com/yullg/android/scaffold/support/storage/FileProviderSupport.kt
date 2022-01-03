package com.yullg.android.scaffold.support.storage

import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.core.content.FileProvider
import com.yullg.android.scaffold.app.Scaffold
import com.yullg.android.scaffold.support.storage.FileProviderSupport.getUriForFile
import com.yullg.android.scaffold.support.storage.FileProviderSupport.importFile
import java.io.File
import java.io.InputStream
import java.util.*

/**
 * 提供与框架默认配置的[FileProvider]的交互接口，简化文件共享流程。
 *
 * 文件首先需要通过[importFile]导入到预设的共享目录中，然后通过[getUriForFile]从共享目录中创建内容URI。
 */
object FileProviderSupport {

    /**
     * 将给定[file]导入由[directory]表示的FileProvider共享目录中，
     * 使用UUID生成唯一的文件名，如果[extension]为NULL，将复用[file]的扩展名，
     * 如果[extension]为空字符串，那么将不生成扩展名，否则使用提供的扩展名。
     */
    @WorkerThread
    fun importFile(
        file: File,
        directory: StorageDirectory,
        extension: String? = null
    ): StorageFile {
        val actualExtension = if (extension == null) {
            file.extension.let { if (it.isEmpty()) "" else ".$it" }
        } else if (extension.isEmpty()) {
            ""
        } else {
            ".$extension"
        }
        val storageFile = StorageFile(
            directory, "yullg/share/${
                UUID.randomUUID().toString().replace("-", "") + actualExtension
            }"
        )
        StorageSupport.copyFrom(file, storageFile)
        return storageFile
    }

    /**
     * 将给定[inputStream]导入由[directory]表示的FileProvider共享目录中，
     * 使用UUID生成唯一的文件名,如果[extension]为NULL或者为空字符串，那么将不生成扩展名，否则使用提供的扩展名。
     */
    @WorkerThread
    fun importFile(
        inputStream: InputStream,
        directory: StorageDirectory,
        extension: String? = null
    ): StorageFile {
        val actualExtension = if (extension == null || extension.isEmpty()) "" else ".$extension"
        val storageFile = StorageFile(
            directory, "yullg/share/${
                UUID.randomUUID().toString().replace("-", "") + actualExtension
            }"
        )
        StorageSupport.copyFrom(inputStream, storageFile)
        return storageFile
    }

    /**
     * 返回给定[file]的内容URI。
     *
     * 注意：[file]必须是已经导入到FileProvider共享目录中的文件（通过[importFile]导入）。
     */
    fun getUriForFile(file: File): Uri =
        FileProvider.getUriForFile(
            Scaffold.context,
            "${Scaffold.context.packageName}.yullg.fileprovider",
            file
        )

    /**
     * 返回给定[file]的内容URI，使用[displayName]替代原始文件名。
     *
     * 注意：[file]必须是已经导入到FileProvider共享目录中的文件（通过[importFile]导入）。
     */
    fun getUriForFile(file: File, displayName: String): Uri =
        FileProvider.getUriForFile(
            Scaffold.context,
            "${Scaffold.context.packageName}.yullg.fileprovider",
            file,
            displayName
        )

}