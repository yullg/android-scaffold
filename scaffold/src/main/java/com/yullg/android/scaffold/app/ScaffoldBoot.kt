package com.yullg.android.scaffold.app

import com.yullg.android.scaffold.support.logger.LogFileDeleteWorker
import com.yullg.android.scaffold.support.logger.LogFileUploadWorker
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
                    is PeriodicLogUploadWorkerOption -> LogFileUploadWorker.enqueuePeriodicWork(it)
                    is OneTimeLogUploadWorkerOption -> LogFileUploadWorker.enqueueOneTimeWork(it)
                }
            }
        } else {
            LogFileUploadWorker.cancelPeriodicWork()
            LogFileUploadWorker.cancelOneTimeWork()
        }
    }

    fun bootDeleteExpiredLog() {
        LogFileDeleteWorker.enqueueOneTimeWork()
    }

}