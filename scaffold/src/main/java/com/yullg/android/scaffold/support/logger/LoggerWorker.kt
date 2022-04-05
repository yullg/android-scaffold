package com.yullg.android.scaffold.support.logger

import android.content.Context
import androidx.work.*
import com.yullg.android.scaffold.app.Scaffold
import com.yullg.android.scaffold.app.ScaffoldConfig
import com.yullg.android.scaffold.app.ScaffoldConstants
import com.yullg.android.scaffold.internal.ScaffoldLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * 托管日志文件上传的[CoroutineWorker]实现
 */
class LogFileUploadWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            ScaffoldLogger.info("[Logger - upload] Begin")
            val logFileUploader = ScaffoldConfig.Logger.uploader
            if (logFileUploader != null) {
                LogFileManager.uploadEachLogFile { logFile ->
                    try {
                        logFileUploader.upload(logFile)
                        ScaffoldLogger.debug("[Logger - upload] Log file has been uploaded: ${logFile.file}")
                        true
                    } catch (e: Exception) {
                        ScaffoldLogger.warn(
                            "[Logger - upload] Failed to upload log file: ${logFile.file}",
                            e
                        )
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
            val workRequest = PeriodicWorkRequestBuilder<LogFileUploadWorker>(
                workerOption.repeatInterval,
                TimeUnit.MILLISECONDS
            ).apply {
                setConstraints(constraints)
                if (workerOption.initialDelay > 0) {
                    setInitialDelay(workerOption.initialDelay, TimeUnit.MILLISECONDS)
                }
            }.build()
            WorkManager.getInstance(Scaffold.context).enqueueUniquePeriodicWork(
                ScaffoldConstants.Logger.WORKER_NAME_UPLOAD_PERIODIC,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
            ScaffoldLogger.info("[Logger] Periodic upload worker has been enqueued: $workerOption")
        }

        /**
         * 安排一次性上传工作
         */
        fun enqueueOneTimeWork(workerOption: OneTimeLogUploadWorkerOption) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(workerOption.requiredNetworkType)
                .build()
            val workRequest = OneTimeWorkRequestBuilder<LogFileUploadWorker>().apply {
                setConstraints(constraints)
                if (workerOption.initialDelay > 0) {
                    setInitialDelay(workerOption.initialDelay, TimeUnit.MILLISECONDS)
                }
            }.build()
            WorkManager.getInstance(Scaffold.context).enqueueUniqueWork(
                ScaffoldConstants.Logger.WORKER_NAME_UPLOAD_ONE_TIME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            ScaffoldLogger.info("[Logger] One-time upload worker has been enqueued: $workerOption")
        }

        /**
         * 取消周期性上传工作
         */
        fun cancelPeriodicWork() {
            WorkManager.getInstance(Scaffold.context)
                .cancelUniqueWork(ScaffoldConstants.Logger.WORKER_NAME_UPLOAD_PERIODIC)
            ScaffoldLogger.info("[Logger] Periodic upload worker has been canceled")
        }

        /**
         * 取消一次性上传工作
         */
        fun cancelOneTimeWork() {
            WorkManager.getInstance(Scaffold.context)
                .cancelUniqueWork(ScaffoldConstants.Logger.WORKER_NAME_UPLOAD_ONE_TIME)
            ScaffoldLogger.info("[Logger] One-time upload worker has been canceled")
        }

    }

}

/**
 * 托管日志文件删除的[CoroutineWorker]实现
 */
class LogFileDeleteWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            ScaffoldLogger.info("[Logger - delete] Begin")
            LogFileManager.deleteExpiredLogFile()
            Result.success()
        } catch (e: Exception) {
            ScaffoldLogger.error("[Logger - delete] Failed", e)
            Result.failure()
        } finally {
            ScaffoldLogger.info("[Logger - delete] End")
        }
    }

    companion object {

        /**
         * 安排一次性删除工作
         */
        fun enqueueOneTimeWork() {
            val workRequest = OneTimeWorkRequest.from(LogFileDeleteWorker::class.java)
            WorkManager.getInstance(Scaffold.context).enqueueUniqueWork(
                ScaffoldConstants.Logger.WORKER_NAME_DELETE_ONE_TIME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            ScaffoldLogger.info("[Logger] One-time delete worker has been enqueued")
        }

        /**
         * 取消一次性删除工作
         */
        fun cancelOneTimeWork() {
            WorkManager.getInstance(Scaffold.context)
                .cancelUniqueWork(ScaffoldConstants.Logger.WORKER_NAME_DELETE_ONE_TIME)
            ScaffoldLogger.info("[Logger] One-time delete worker has been canceled")
        }

    }

}