package com.yullg.android.scaffold.support.media

object GlobalSoundPool {

    private val soundPoolProxy: SoundPoolProxy by lazy {
        SoundPoolProxy(
            maxStreams = GlobalSoundPoolConfig.maxStreams,
            usage = GlobalSoundPoolConfig.usage,
            contentType = GlobalSoundPoolConfig.contentType,
            flags = GlobalSoundPoolConfig.flags
        ).apply {
            GlobalSoundPoolConfig.sounds?.forEach { sound ->
                registerSound(sound)
            }
        }
    }

    fun load(id: Int) = soundPoolProxy.load(id)

    fun unload(id: Int) = soundPoolProxy.unload(id)

    fun play(id: Int, volume: Float = 1f, loop: Int = 0) = soundPoolProxy.play(id, volume, loop)

    fun resume(id: Int) = soundPoolProxy.resume(id)

    fun pause(id: Int) = soundPoolProxy.pause(id)

    fun stop(id: Int) = soundPoolProxy.stop(id)

    fun autoPause() = soundPoolProxy.autoPause()

    fun autoResume() = soundPoolProxy.autoResume()

}

open class GlobalSoundPoolConfig private constructor() {

    var maxStreams: Int? = null
    var usage: Int? = null
    var contentType: Int? = null
    var flags: Int? = null
    var sounds: List<Sound>? = null

    internal companion object : GlobalSoundPoolConfig()

}