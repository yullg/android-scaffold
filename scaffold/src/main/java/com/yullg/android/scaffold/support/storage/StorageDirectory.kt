package com.yullg.android.scaffold.support.storage

import com.yullg.android.scaffold.app.Scaffold
import java.io.File

/**
 * 定义应用程序常用的几种存储目录
 */
enum class StorageDirectory {

    /**
     * 应用程序内部存储区的files/目录，与[Context.getFilesDir()]相同。
     */
    FILES,

    /**
     * 应用程序内部存储区的cache/目录，与[Context.getCacheDir()]相同。
     */
    CACHE,

    /**
     * 应用程序外部存储区的files/目录，与[Context.getExternalFilesDir(null)]相同。
     */
    EXTERNAL_FILES,

    /**
     * 应用程序外部存储区的cache/目录，与[Context.getExternalCacheDir()]相同。
     */
    EXTERNAL_CACHE

}

/**
 * 获取存储目录的[File]表示形式
 *
 * @throws NullPointerException 如果相应存储区不可用
 */
val StorageDirectory.file: File
    get() = when (this) {
        StorageDirectory.FILES -> Scaffold.context.filesDir
        StorageDirectory.CACHE -> Scaffold.context.cacheDir
        StorageDirectory.EXTERNAL_FILES -> Scaffold.context.getExternalFilesDir(null)
            ?: throw NullPointerException("Shared storage is not currently available")
        StorageDirectory.EXTERNAL_CACHE -> Scaffold.context.externalCacheDir
            ?: throw NullPointerException("Shared storage is not currently available")
    }