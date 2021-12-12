package com.yullg.android.scaffold.support.logger

import android.content.Context
import androidx.work.*
import com.yullg.android.scaffold.app.Scaffold
import com.yullg.android.scaffold.app.ScaffoldConfig
import com.yullg.android.scaffold.app.ScaffoldConstants
import com.yullg.android.scaffold.helper.SettingsHelper
import com.yullg.android.scaffold.internal.AliyunOSSClient
import com.yullg.android.scaffold.internal.ScaffoldLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URI
import java.util.concurrent.TimeUnit

/**
 * 一个[CoroutineWorker]实现，用于托管日志上传工作。
 */
class LogUploadWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            ScaffoldLogger.info("[Logger - upload] Begin")
            val logUploader = ScaffoldConfig.Logger.uploader
            if (logUploader != null) {
                LogFileUtil.uploadEachLogFile { file ->
                    try {
                        file.inputStream().use { fis ->
                            logUploader.upload(file.name, fis)
                        }
                        ScaffoldLogger.debug("[Logger - upload] Log file has been uploaded: $file")
                        true
                    } catch (e: Exception) {
                        ScaffoldLogger.warn("[Logger - upload] Log file upload failed: $file", e)
                        false
                    }
                }
                ScaffoldLogger.debug("[Logger - upload] All log files tried")
            } else {
                ScaffoldLogger.warn("[Logger - upload] Uploader is not provided")
            }
            Result.success()
        } catch (e: Exception) {
            ScaffoldLogger.error("[Logger - upload] Failed", e)
            Result.failure()
        } finally {
            ScaffoldLogger.info("[Logger - upload] End")
        }
    }

    companion object {

        /**
         * 安排周期性上传工作
         */
        fun enqueuePeriodicWork(workerOption: PeriodicLogUploadWorkerOption) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(workerOption.requiredNetworkType)
                .build()
            val workRequest = PeriodicWorkRequestBuilder<LogUploadWorker>(
                workerOption.repeatInterval,
                TimeUnit.MILLISECONDS
            ).apply {
                setConstraints(constraints)
                if (workerOption.initialDelay > 0) {
                    setInitialDelay(workerOption.initialDelay, TimeUnit.MILLISECONDS)
                }
            }.build()
            WorkManager.getInstance(Scaffold.context).enqueueUniquePeriodicWork(
                ScaffoldConstants.Logger.WORKER_NAME_UPLOADER_PERIODIC,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
            ScaffoldLogger.info("[Logger] Periodic upload work has been enqueued: workerOption = $workerOption")
        }

        /**
         * 安排一次性上传工作
         */
        fun enqueueOneTimeWork(workerOption: OneTimeLogUploadWorkerOption) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(workerOption.requiredNetworkType)
                .build()
            val workRequest = OneTimeWorkRequestBuilder<LogUploadWorker>().apply {
                setConstraints(constraints)
                if (workerOption.initialDelay > 0) {
                    setInitialDelay(workerOption.initialDelay, TimeUnit.MILLISECONDS)
                }
            }.build()
            WorkManager.getInstance(Scaffold.context).enqueueUniqueWork(
                ScaffoldConstants.Logger.WORKER_NAME_UPLOADER_ONE_TIME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            ScaffoldLogger.info("[Logger] One-time upload work has been enqueued: workerOption = $workerOption")
        }

        /**
         * 取消周期性上传工作
         */
        fun cancelPeriodicWork() {
            WorkManager.getInstance(Scaffold.context)
                .cancelUniqueWork(ScaffoldConstants.Logger.WORKER_NAME_UPLOADER_PERIODIC)
            ScaffoldLogger.info("[Logger] Periodic upload work has been canceled")
        }

        /**
         * 取消一次性上传工作
         */
        fun cancelOneTimeWork() {
            WorkManager.getInstance(Scaffold.context)
                .cancelUniqueWork(ScaffoldConstants.Logger.WORKER_NAME_UPLOADER_ONE_TIME)
            ScaffoldLogger.info("[Logger] One-time upload work has been canceled")
        }

    }

}

/**
 * 定义日志文件上传接口
 */
interface LogUploader {

    /**
     * 执行文件上传操作。该方法正常结束表示上传成功；该方法异常结束表示上传失败。
     */
    fun upload(filename: String, inputStream: InputStream)

}

/**
 * 一个[LogUploader]实现，提供将日志文件上传到阿里云OSS的功能。
 */
class AliyunOSSLogUploader(
    endpointURI: URI,
    accessKeyId: String,
    accessKeySecret: String,
    private val bucketName: String,
    private val objectKeyGetter: (String) -> String = { "${SettingsHelper.SSAID}/$it" }
) : LogUploader {

    private val ossClient = AliyunOSSClient(endpointURI, accessKeyId, accessKeySecret)

    override fun upload(name: String, inputStream: InputStream) {
        ossClient.appendObject(bucketName, objectKeyGetter(name), inputStream)
    }

}