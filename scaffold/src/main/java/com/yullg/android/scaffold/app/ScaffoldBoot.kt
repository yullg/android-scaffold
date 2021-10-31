package com.yullg.android.scaffold.app

import androidx.work.*
import com.yullg.android.scaffold.internal.ScaffoldLogger
import com.yullg.android.scaffold.support.logger.Appender
import com.yullg.android.scaffold.support.logger.LogUploadWorker
import java.util.concurrent.TimeUnit

object ScaffoldBoot {

    fun bootAll() {
        bootUploadLog()
        bootDeleteExpiredLog()
    }

    fun bootUploadLog() {
        val uploader = ScaffoldConfig.Logger.uploader
        if (uploader != null) {
            ScaffoldLogger.info("[LogUpload] LogUploader has provided, enqueue worker")
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = PeriodicWorkRequestBuilder<LogUploadWorker>(
                uploader.repeatInterval,
                TimeUnit.MILLISECONDS
            ).setInitialDelay(
                uploader.initialDelay,
                TimeUnit.MILLISECONDS
            ).setConstraints(constraints)
                .build()
            WorkManager.getInstance(Scaffold.context).enqueueUniquePeriodicWork(
                ScaffoldConstants.Logger.WORKER_NAME_UPLOADER,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
            ScaffoldLogger.info("[LogUpload] Worker enqueued")
        } else {
            ScaffoldLogger.info("[LogUpload] LogUploader not provided, cancel worker")
            WorkManager.getInstance(Scaffold.context)
                .cancelUniqueWork(ScaffoldConstants.Logger.WORKER_NAME_UPLOADER)
            ScaffoldLogger.info("[LogUpload] Worker cancelled")
        }
    }

    fun bootDeleteExpiredLog() {
        Appender.deleteLog(ScaffoldConfig.Logger.logFileMaxLife).let {
            ScaffoldLogger.info(
                "[LogAppender] Ready to delete expired log files : logFileMaxLife = ${
                    ScaffoldConfig.Logger.logFileMaxLife
                }, result = $it"
            )
        }
    }

}