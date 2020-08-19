package com.yullg.android.scaffold.app

import com.yullg.android.scaffold.support.logger.MutableLoggerConfig
import com.yullg.android.scaffold.support.media.MutableMediaConfig
import com.yullg.android.scaffold.ui.MutableUIConfig

class ScaffoldConfig internal constructor() {

    fun logger(block: MutableLoggerConfig.() -> Unit): ScaffoldConfig {
        block(MutableLoggerConfig)
        return this
    }

    fun media(block: MutableMediaConfig.() -> Unit): ScaffoldConfig {
        block(MutableMediaConfig)
        return this
    }

    fun ui(block: MutableUIConfig.() -> Unit): ScaffoldConfig {
        block(MutableUIConfig)
        return this
    }

}