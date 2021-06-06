package com.yullg.android.scaffold.support.logger

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yullg.android.scaffold.helper.DateHelper
import com.yullg.android.scaffold.helper.SystemHelper
import com.yullg.android.scaffold.internal.AliyunOSSClient
import com.yullg.android.scaffold.internal.ScaffoldLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URI

class LogUploadWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            ScaffoldLogger.info("[LogUpload - Worker] begin")
            val logUploader = LoggerConfig.uploader
            if (logUploader != null) {
                LogFileUtil.uploadEachLogFile { file ->
                    try {
                        ScaffoldLogger.debug("[LogUpload - Worker] upload : $file")
                        file.inputStream().use { fis ->
                            logUploader.upload(file.name, fis)
                        }
                        ScaffoldLogger.debug("[LogUpload - Worker] upload succeeded : $file")
                        true
                    } catch (e: Exception) {
                        ScaffoldLogger.warn("[LogUpload - Worker] upload failed : $file", e)
                        false
                    }
                }
                ScaffoldLogger.debug("[LogUpload - Worker] End of the upload")
            } else {
                ScaffoldLogger.debug("[LogUpload - Worker] Uploader is not provided")
            }
            Result.success()
        } catch (e: Exception) {
            ScaffoldLogger.error("[LogUpload - Worker] error", e)
            Result.failure()
        } finally {
            ScaffoldLogger.info("[LogUpload - Worker] end")
        }
    }

}

interface LogUploader {

    val initialDelay: Long

    val repeatInterval: Long

    fun upload(name: String, inputStream: InputStream)

}

class AliyunOSSLogUploader(
    endpointURI: URI,
    accessKeyId: String,
    accessKeySecret: String,
    private val bucketName: String,
    private val objectKeyGetter: (String) -> String = { "${SystemHelper.SSAID}/$it" }
) : LogUploader {

    override var initialDelay: Long = 0

    override var repeatInterval: Long = 12 * DateHelper.MILLIS_PER_HOUR

    private val ossClient = AliyunOSSClient(endpointURI, accessKeyId, accessKeySecret)

    override fun upload(name: String, inputStream: InputStream) {
        ossClient.appendObject(bucketName, objectKeyGetter(name), inputStream)
    }

}