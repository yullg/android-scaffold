package com.yullg.android.scaffold.app

import android.content.Context
import androidx.startup.Initializer
import androidx.work.WorkManagerInitializer
import com.yullg.android.scaffold.internal.ScaffoldLogger
import com.yullg.android.scaffold.support.logger.bootDeleteExpiredLog
import com.yullg.android.scaffold.support.logger.bootUploadLog

open class ScaffoldInitializer : Initializer<Any> {

    final override fun create(context: Context): Any {
        try {
            Scaffold.activate(context)
            configure(context, ScaffoldConfig())
            bootUploadLog(context)
            bootDeleteExpiredLog(context)
            onInitialized(context)
            ScaffoldLogger.info("[ScaffoldInitializer] Initialization succeeded")
            return Unit
        } catch (e: Throwable) {
            ScaffoldLogger.fatal("[ScaffoldInitializer] Initialization failed", e)
            throw e
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(WorkManagerInitializer::class.java)
    }

    protected open fun configure(context: Context, config: ScaffoldConfig) {}

    protected open fun onInitialized(context: Context) {}

}

// If use Initializer<Unit> instead of Initializer<Any>, cannot use it in a Java environment
// ref : https://youtrack.jetbrains.com/issue/KT-15964