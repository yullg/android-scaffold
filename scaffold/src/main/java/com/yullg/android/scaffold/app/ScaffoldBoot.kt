package com.yullg.android.scaffold.app

import com.yullg.android.scaffold.internal.ScaffoldLogger
import com.yullg.android.scaffold.support.logger.LogManager
import com.yullg.android.scaffold.support.logger.LogUploadWorker
import com.yullg.android.scaffold.support.logger.OneTimeLogUploadWorkerOption
import com.yullg.android.scaffold.support.logger.PeriodicLogUploadWorkerOption

object ScaffoldBoot {

    fun bootAll() {
        bootUploadLog()
        bootDeleteExpiredLog()
    }

    fun bootUploadLog() {
        val uploader = ScaffoldConfig.Logger.uploader
        if (uploader != null) {
            ScaffoldConfig.Logger.logUploadWorkerOption.let {
                when (it) {
                    is PeriodicLogUploadWorkerOption -> LogUploadWorker.enqueuePeriodicWork(it)
                    is OneTimeLogUploadWorkerOption -> LogUploadWorker.enqueueOneTimeWork(it)
                }
            }
        } else {
            LogUploadWorker.cancelPeriodicWork()
            LogUploadWorker.cancelOneTimeWork()
        }
    }

    fun bootDeleteExpiredLog() {
        LogManager.deleteLog(ScaffoldConfig.Logger.logFileMaxLife).let {
            ScaffoldLogger.info(
                "[Logger] Ready to delete expired log files : logFileMaxLife = ${
                    ScaffoldConfig.Logger.logFileMaxLife
                }, result = $it"
            )
        }
    }

}