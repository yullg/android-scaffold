package com.yullg.android.scaffold.support.storage

import androidx.core.content.FileProvider
import com.yullg.android.scaffold.app.Scaffold
import java.io.File
import java.util.*

object FileProviderSupport {

    fun importFile(
        file: File,
        path: String,
        fileName: String? = null,
        fileExtension: String? = null
    ): File {
        val targetFile = File(path,
            (fileName ?: UUID.randomUUID().toString()) +
                    (fileExtension ?: file.extension).let { if (it.isBlank()) "" else ".$it" }
        )
        return file.copyTo(targetFile)
    }

    fun getUriForFile(file: File, authorities: String) =
        FileProvider.getUriForFile(Scaffold.context, authorities, file)

    fun getUriForFile(file: File, displayName: String, authorities: String) =
        FileProvider.getUriForFile(Scaffold.context, authorities, file, displayName)

}