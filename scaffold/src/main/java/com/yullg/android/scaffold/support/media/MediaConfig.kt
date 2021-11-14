package com.yullg.android.scaffold.support.media

interface MediaConfig {

    val globalSoundPoolOption: GlobalSoundPoolOption

}

open class MutableMediaConfig private constructor() : MediaConfig {

    override var globalSoundPoolOption: GlobalSoundPoolOption = GlobalSoundPoolOption()

    internal companion object : MutableMediaConfig()

}