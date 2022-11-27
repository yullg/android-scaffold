package com.yullg.android.scaffold.support.storage

import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.core.content.FileProvider
import com.yullg.android.scaffold.app.Scaffold
import com.yullg.android.scaffold.support.storage.ShareFileSupport.getUriForFile
import com.yullg.android.scaffold.support.storage.ShareFileSupport.importFile
import java.io.File
import java.io.InputStream
import java.util.*

/**
 * 提供向其他应用分享文件的功能，底层使用Android [FileProvider]组件实现，具体操作流程如下：
 * 1. 调用[importFile]将要分享的文件导入预定义的分享目录下。框架已经在[StorageDirectory]可表示的
 * 各种存储目录下预定义了子目录作为分享目录。暂不支持添加其他分享目录。
 * 2. 调用[getUriForFile]从已导入到分享目录中的文件中生成content URI。
 * 3. 对从第二步获取的content URI授权（此类不包含这一步骤的相关代码，它由调用者自行处理）。授权方式分为：
 *    - 临时授权：将content URI和权限标志添加到`Intent`。
 *    - 永久授权：使用`Context.grantUriPermission()`授权，使用`Context.revokeUriPermission()`撤消授权。
 */
object ShareFileSupport {

    /**
     * 将给定[file]导入由[directory]表示的目录下的共享目录中，
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
     * 将给定[inputStream]导入由[directory]表示的目录下的共享目录中，
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
     * 返回给定[file]的content URI。
     *
     * 注意：[file]必须是已经导入到共享目录中的文件（通过[importFile]导入）。
     */
    fun getUriForFile(file: File): Uri =
        FileProvider.getUriForFile(
            Scaffold.context,
            "${Scaffold.context.packageName}.yullg.scaffoldfileprovider",
            file
        )

    /**
     * 返回给定[file]的content URI，使用[displayName]替代原始文件名。
     *
     * 注意：[file]必须是已经导入到共享目录中的文件（通过[importFile]导入）。
     */
    fun getUriForFile(file: File, displayName: String): Uri =
        FileProvider.getUriForFile(
            Scaffold.context,
            "${Scaffold.context.packageName}.yullg.scaffoldfileprovider",
            file,
            displayName
        )

}