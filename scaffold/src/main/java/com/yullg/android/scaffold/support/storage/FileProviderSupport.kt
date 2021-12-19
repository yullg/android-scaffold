package com.yullg.android.scaffold.support.storage

import androidx.annotation.WorkerThread
import androidx.core.content.FileProvider
import com.yullg.android.scaffold.app.Scaffold
import java.io.File
import java.util.*

/**
 * 提供与框架默认配置的[FileProvider]的交互接口，简化文件共享流程。
 *
 * 文件首先需要通过[importFile()]导入到预设的共享目录中，然后通过[getUriForFile()]从共享目录中创建内容URI。
 */
object FileProviderSupport {

    /**
     * 将给定[file]导入由[directory]表示的FileProvider共享目录中，使用UUID生成唯一的文件名。
     */
    @WorkerThread
    fun importFile(file: File, directory: FileProviderDirectory): File {
        val targetFile = newShareFile(
            directory,
            UUID.randomUUID().toString().replace("-", "")
                    + file.extension.let { if (it.isEmpty()) "" else ".$it" }
        )
        return file.copyTo(targetFile)
    }

    /**
     * 返回给定[file]的内容URI。
     *
     * 注意：[file]必须是已经导入到FileProvider共享目录中的文件（通过[importFile]导入）。
     */
    fun getUriForFile(file: File) =
        FileProvider.getUriForFile(
            Scaffold.context,
            "${Scaffold.context.packageName}.yg.fileprovider",
            file
        )

    /**
     * 返回给定[file]的内容URI，使用[displayName]替代原始文件名。
     *
     * 注意：[file]必须是已经导入到FileProvider共享目录中的文件（通过[importFile]导入）。
     */
    fun getUriForFile(file: File, displayName: String) =
        FileProvider.getUriForFile(
            Scaffold.context,
            "${Scaffold.context.packageName}.yg.fileprovider",
            file,
            displayName
        )

    /**
     * 根据给定的FileProvider目录和文件名创建File实例
     */
    private fun newShareFile(fileProviderDirectory: FileProviderDirectory, filename: String): File {
        return when (fileProviderDirectory) {
            FileProviderDirectory.FILES -> File(Scaffold.context.filesDir, filename)
            FileProviderDirectory.CACHE -> File(Scaffold.context.cacheDir, filename)
            FileProviderDirectory.EXTERNAL_FILES -> File(
                Scaffold.context.getExternalFilesDir(null),
                filename
            )
            FileProviderDirectory.EXTERNAL_CACHE -> File(
                Scaffold.context.externalCacheDir,
                filename
            )
        }
    }

}

/**
 * FileProvider共享文件的所属目录
 */
enum class FileProviderDirectory { FILES, CACHE, EXTERNAL_FILES, EXTERNAL_CACHE }