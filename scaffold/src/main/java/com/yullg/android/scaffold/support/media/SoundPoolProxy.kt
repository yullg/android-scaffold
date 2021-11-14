package com.yullg.android.scaffold.support.media

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.FileDescriptor
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * 在[SoundPool]基础上提供便捷的方式维护音频数据和控制音频播放。
 */
class SoundPoolProxy(
    private val maxStreams: Int? = null,
    private val usage: Int? = null,
    private val contentType: Int? = null,
    private val flags: Int? = null
) {

    private val soundPool: SoundPool by lazy {
        SoundPool.Builder().apply {
            if (maxStreams != null) setMaxStreams(maxStreams)
            setAudioAttributes(AudioAttributes.Builder().apply {
                if (usage != null) setUsage(usage)
                if (contentType != null) setContentType(contentType)
                if (flags != null) setFlags(flags)
            }.build())
        }.build().apply {
            setOnLoadCompleteListener(onLoadCompleteListener)
        }
    }

    private val sounds = ConcurrentHashMap<Int, Sound>()

    /**
     * 注册音源
     */
    fun registerSound(sound: Sound) {
        sounds[sound.id] = sound
    }

    /**
     * 取消注册音源
     */
    fun unregisterSound(id: Int) {
        val sound = sounds[id] ?: return
        try {
            // 如果音源已经加载，执行卸载...
            if (SoundState.NOT_LOADED != sound.state) {
                soundPool.unload(sound.soundId)
            }
        } finally {
            sounds.remove(id)
        }
    }

    /**
     * 加载音源
     */
    fun load(id: Int) {
        val sound =
            sounds[id] ?: throw IllegalArgumentException("Unregistered sound requested: $id")
        if (SoundState.NOT_LOADED == sound.state) {
            synchronized(sound) {
                if (SoundState.NOT_LOADED == sound.state) {
                    loadSound(sound)
                }
            }
        }
    }

    /**
     * 卸载音源
     */
    fun unload(id: Int) {
        val sound = sounds[id] ?: return
        if (SoundState.NOT_LOADED != sound.state) {
            synchronized(sound) {
                if (SoundState.NOT_LOADED != sound.state) {
                    try {
                        soundPool.unload(sound.soundId)
                    } finally {
                        sound.state = SoundState.NOT_LOADED
                        sound.soundId = 0
                        sound.streamId = 0
                    }
                }
            }
        }
    }

    /**
     * 播放音源
     */
    fun play(id: Int, volume: Float = 1f, loop: Int = 0) {
        val sound =
            sounds[id] ?: throw IllegalArgumentException("Unregistered sound requested: $id")
        synchronized<Unit>(sound) {
            when (sound.state) {
                SoundState.NOT_LOADED -> {
                    loadSound(sound)
                    if (SoundState.LOADING == sound.state) {
                        sound.state = SoundState.LOADING_PLAY_REQUESTED
                        sound.playVolume = volume
                        sound.playLoop = loop
                    }
                }
                SoundState.LOADING -> {
                    sound.state = SoundState.LOADING_PLAY_REQUESTED
                    sound.playVolume = volume
                    sound.playLoop = loop
                }
                SoundState.LOADING_PLAY_REQUESTED -> {
                    sound.playVolume = volume
                    sound.playLoop = loop
                }
                SoundState.STATE_LOADED -> {
                    soundPool.play(sound.soundId, volume, volume, 0, loop, 1f).also {
                        sound.streamId = it
                    }
                }
            }
        }
    }

    /**
     * 恢复播放音源
     */
    fun resume(id: Int) {
        val sound = sounds[id] ?: throw RuntimeException("Unregistered sound requested: $id")
        val streamId = sound.streamId
        if (streamId != 0) {
            soundPool.resume(streamId)
        }
    }

    /**
     * 暂停播放音源
     */
    fun pause(id: Int) {
        val sound = sounds[id] ?: return
        val streamId = sound.streamId
        if (streamId != 0) {
            soundPool.pause(streamId)
        }
    }

    /**
     * 停止播放音源
     */
    fun stop(id: Int) {
        val sound = sounds[id] ?: return
        val streamId = sound.streamId
        if (streamId != 0) {
            soundPool.stop(streamId)
        }
    }

    /**
     * 暂停所有播放的音源
     */
    fun autoPause() = soundPool.autoPause()

    /**
     * 恢复所有播放的音源
     */
    fun autoResume() = soundPool.autoResume()

    /**
     * 释放所有资源
     */
    fun release() = soundPool.release()

    /**
     * 启动音源加载流程，启动成功后将音源状态标记为加载中(LOADING)并记录`Sound ID`。
     */
    private fun loadSound(sound: Sound) {
        when (sound) {
            is ResourceSound -> {
                soundPool.load(
                    sound.contextRef.get() ?: throw RuntimeException("Context has been reclaimed"),
                    sound.resId,
                    1
                ).also {
                    if (it > 0) {
                        sound.state = SoundState.LOADING
                        sound.soundId = it
                    }
                }
            }
            is FileSound -> {
                soundPool.load(sound.path, 1).also {
                    if (it > 0) {
                        sound.state = SoundState.LOADING
                        sound.soundId = it
                    }
                }
            }
            is AssetFileDescriptorSound -> {
                soundPool.load(sound.afd, 1).also {
                    if (it > 0) {
                        sound.state = SoundState.LOADING
                        sound.soundId = it
                    }
                }
            }
            is FileDescriptorSound -> {
                soundPool.load(sound.fd, sound.offset, sound.length, 1).also {
                    if (it > 0) {
                        sound.state = SoundState.LOADING
                        sound.soundId = it
                    }
                }
            }
        }
    }

    /**
     * 音源加载状态监听器
     */
    private val onLoadCompleteListener =
        SoundPool.OnLoadCompleteListener { soundPool, sampleId, status ->
            for (sound in sounds.values) {
                if (sound.soundId != sampleId) {
                    continue
                }
                synchronized(sound) {
                    if (status != 0) {
                        // 加载失败了，回滚状态...
                        sound.state = SoundState.NOT_LOADED
                        sound.playLoop = 0
                        sound.soundId = 0
                        return@synchronized
                    }
                    when (sound.state) {
                        SoundState.LOADING -> sound.state = SoundState.STATE_LOADED
                        SoundState.LOADING_PLAY_REQUESTED -> {
                            sound.state = SoundState.STATE_LOADED
                            // 在加载同时也请求播放，那么在加载完成后直接开始播放...
                            soundPool.play(
                                sound.soundId,
                                sound.playVolume,
                                sound.playVolume,
                                0,
                                sound.playLoop,
                                1f
                            ).also {
                                sound.streamId = it
                            }
                        }
                    }
                }
                break
            }
        }

}

/**
 * 音源([Sound])的状态
 */
internal enum class SoundState { NOT_LOADED, LOADING, LOADING_PLAY_REQUESTED, STATE_LOADED }

/**
 * 封装[SoundPoolProxy]的音源，并用于跟踪状态信息。
 *
 * 注意：[Sound]实例不能在多个[SoundPoolProxy]间共享。
 */
sealed class Sound(val id: Int) {
    internal var state: SoundState = SoundState.NOT_LOADED
    internal var soundId: Int = 0
    internal var streamId: Int = 0
    internal var playVolume: Float = 1f
    internal var playLoop: Int = 0
}

/**
 * 一个[Sound]实现，用于从APK资源加载音源数据。
 */
class ResourceSound(id: Int, context: Context, val resId: Int) : Sound(id) {
    val contextRef = WeakReference(context)
}

/**
 * 一个[Sound]实现，用于从本地文件系统路径加载音源数据。
 */
class FileSound(id: Int, val path: String) : Sound(id)


/**
 * 一个[Sound]实现，用于从[AssetFileDescriptor]加载音源数据。
 */
class AssetFileDescriptorSound(id: Int, val afd: AssetFileDescriptor) : Sound(id)

/**
 * 一个[Sound]实现，用于从[FileDescriptor]加载音源数据。
 */
class FileDescriptorSound(
    id: Int,
    val fd: FileDescriptor,
    val offset: Long,
    val length: Long
) : Sound(id)