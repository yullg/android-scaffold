package com.yullg.android.scaffold.support.storage

import java.io.File

/**
 * 表示存储在由[StorageDirectory]定义的存储目录中的文件，并且提供与`String`表示形式的互相转换来避免依赖绝对路径。
 */
class StorageFile(
    val storageDirectory: StorageDirectory,
    val relativePath: String
) {

    val file: File = File(storageDirectory.file, relativePath)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as StorageFile
        if (storageDirectory != other.storageDirectory) return false
        if (relativePath != other.relativePath) return false
        return true
    }

    override fun hashCode(): Int {
        var result = storageDirectory.hashCode()
        result = 31 * result + relativePath.hashCode()
        return result
    }

    /**
     * 返回这个[StorageFile]实例的`String`表示形式，如下所示：
     *
     * `storageDirectory.name + ":" + relativePath`
     */
    override fun toString(): String {
        return "${storageDirectory.name}:$relativePath"
    }

    companion object {

        fun from(source: String): StorageFile {
            val index = source.indexOf(":")
            if (index < 0) throw IllegalArgumentException("Source is not a string of StorageFile: $source")
            return StorageFile(
                StorageDirectory.valueOf(source.substring(0, index)),
                source.substring(index + 1, source.length)
            )
        }

        fun from(source: File): StorageFile {
            val storageDirectory: StorageDirectory
            val relativePath: String
            if (source.startsWith(StorageDirectory.FILES.file)) {
                storageDirectory = StorageDirectory.FILES
                relativePath = source.toRelativeString(StorageDirectory.FILES.file)
            } else if (source.startsWith(StorageDirectory.CACHE.file)) {
                storageDirectory = StorageDirectory.CACHE
                relativePath = source.toRelativeString(StorageDirectory.CACHE.file)
            } else if (source.startsWith(StorageDirectory.EXTERNAL_FILES.file)) {
                storageDirectory = StorageDirectory.EXTERNAL_FILES
                relativePath = source.toRelativeString(StorageDirectory.EXTERNAL_FILES.file)
            } else if (source.startsWith(StorageDirectory.EXTERNAL_CACHE.file)) {
                storageDirectory = StorageDirectory.EXTERNAL_CACHE
                relativePath = source.toRelativeString(StorageDirectory.EXTERNAL_CACHE.file)
            } else throw IllegalArgumentException("Source is not a file of StorageFile: $source")
            return StorageFile(storageDirectory, relativePath)
        }

    }

}