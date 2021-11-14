package com.yullg.android.scaffold.support.media

import com.yullg.android.scaffold.app.ScaffoldConfig

/**
 * 一个[SoundPoolProxy]的全局代理实例，在首次使用时自动读取配置进行初始化，提供开箱即用的音频播放功能。
 */
object GlobalSoundPool {

    private val soundPoolProxy: SoundPoolProxy by lazy {
        SoundPoolProxy(
            maxStreams = ScaffoldConfig.Media.globalSoundPoolOption.maxStreams,
            usage = ScaffoldConfig.Media.globalSoundPoolOption.usage,
            contentType = ScaffoldConfig.Media.globalSoundPoolOption.contentType,
            flags = ScaffoldConfig.Media.globalSoundPoolOption.flags
        ).apply {
            ScaffoldConfig.Media.globalSoundPoolOption.sounds?.forEach { sound ->
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

/**
 * 定义[GlobalSoundPool]实例的配置选项
 */
data class GlobalSoundPoolOption(
    val maxStreams: Int? = null,
    val usage: Int? = null,
    val contentType: Int? = null,
    val flags: Int? = null,
    val sounds: List<Sound>? = null
)