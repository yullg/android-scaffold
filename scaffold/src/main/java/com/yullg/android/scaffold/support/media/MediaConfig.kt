package com.yullg.android.scaffold.support.media

interface MediaConfig

open class MutableMediaConfig private constructor() : MediaConfig {

    fun globalSoundPool(block: GlobalSoundPoolConfig.() -> Unit) {
        GlobalSoundPoolConfig.block()
    }

    internal companion object : MutableMediaConfig()

}