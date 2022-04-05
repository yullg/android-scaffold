package com.yullg.android.scaffold.app

import android.content.Context
import androidx.startup.Initializer
import androidx.work.WorkManagerInitializer
import com.yullg.android.scaffold.internal.ScaffoldLogger

/**
 * 框架初始化器
 */
open class ScaffoldInitializer : Initializer<Any> {

    final override fun create(context: Context): Any {
        try {
            Scaffold.activate(context)
            configure(context, ScaffoldConfig())
            onInitialized(context)
            ScaffoldLogger.info("[ScaffoldInitializer] Initialize succeeded")
            return Unit
        } catch (e: Throwable) {
            ScaffoldLogger.fatal("[ScaffoldInitializer] Initialize failed", e)
            throw e
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(WorkManagerInitializer::class.java)
    }

    protected open fun configure(context: Context, config: ScaffoldConfig) {}

    protected open fun onInitialized(context: Context) {
        ScaffoldBoot.bootAll()
    }

}

// If use Initializer<Unit> instead of Initializer<Any>, cannot use it in a Java environment
// ref : https://youtrack.jetbrains.com/issue/KT-15964