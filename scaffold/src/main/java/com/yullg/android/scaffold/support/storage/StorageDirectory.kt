package com.yullg.android.scaffold.support.storage

import com.yullg.android.scaffold.app.Scaffold
import java.io.File

/**
 * 存储目录
 */
enum class StorageDirectory {

    /**
     * 表示应用程序内部存储区域的files/目录。该目录与[Context.getFilesDir()]返回的值相同。
     */
    FILES,

    /**
     * 表示应用程序内部存储区域的cache/目录。该目录与[Context.getCacheDir()]返回的值相同。
     */
    CACHE,

    /**
     * 表示应用程序外部存储区域的files/目录。该目录与[Context.getExternalFilesDir(null)]返回的值相同。
     */
    EXTERNAL_FILES,

    /**
     * 表示应用程序外部存储区域的cache/目录。该目录与[Context.getExternalCacheDir()]返回的值相同。
     */
    EXTERNAL_CACHE

}

/**
 * 获取存储目录的[File]实例
 *
 * @throws NullPointerException 如果相应存储区不可用
 */
val StorageDirectory.file: File
    get() = when (this) {
        StorageDirectory.FILES -> Scaffold.context.filesDir
        StorageDirectory.CACHE -> Scaffold.context.cacheDir
        StorageDirectory.EXTERNAL_FILES -> Scaffold.context.getExternalFilesDir(null)
            ?: throw NullPointerException("Storage is not currently available")
        StorageDirectory.EXTERNAL_CACHE -> Scaffold.context.externalCacheDir
            ?: throw NullPointerException("Storage is not currently available")
    }