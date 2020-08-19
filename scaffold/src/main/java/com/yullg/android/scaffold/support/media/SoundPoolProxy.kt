package com.yullg.android.scaffold.support.media

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.FileDescriptor
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

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

    fun registerSound(sound: Sound) {
        sounds[sound.id] = sound
    }

    fun unregisterSound(id: Int) {
        val sound = sounds[id] ?: return
        try {
            if (SoundState.NOT_LOADED != sound.state) {
                soundPool.unload(sound.soundId)
            }
        } finally {
            sounds.remove(id)
        }
    }

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

    fun resume(id: Int) {
        val sound = sounds[id] ?: throw RuntimeException("Unregistered sound requested: $id")
        val streamId = sound.streamId
        if (streamId != 0) {
            soundPool.resume(streamId)
        }
    }

    fun pause(id: Int) {
        val sound = sounds[id] ?: return
        val streamId = sound.streamId
        if (streamId != 0) {
            soundPool.pause(streamId)
        }
    }

    fun stop(id: Int) {
        val sound = sounds[id] ?: return
        val streamId = sound.streamId
        if (streamId != 0) {
            soundPool.stop(streamId)
        }
    }

    fun autoPause() = soundPool.autoPause()

    fun autoResume() = soundPool.autoResume()

    fun release() = soundPool.release()

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

    private val onLoadCompleteListener =
        SoundPool.OnLoadCompleteListener { soundPool, sampleId, status ->
            for (sound in sounds.values) {
                if (sound.soundId != sampleId) {
                    continue
                }
                synchronized(sound) {
                    if (status != 0) {
                        sound.state = SoundState.NOT_LOADED
                        sound.playLoop = 0
                        sound.soundId = 0
                        return@synchronized
                    }
                    when (sound.state) {
                        SoundState.LOADING -> sound.state = SoundState.STATE_LOADED
                        SoundState.LOADING_PLAY_REQUESTED -> {
                            sound.state = SoundState.STATE_LOADED
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

internal enum class SoundState { NOT_LOADED, LOADING, LOADING_PLAY_REQUESTED, STATE_LOADED }

sealed class Sound(val id: Int) {
    internal var state: SoundState = SoundState.NOT_LOADED
    internal var soundId: Int = 0
    internal var streamId: Int = 0
    internal var playVolume: Float = 1f
    internal var playLoop: Int = 0
}

class ResourceSound(id: Int, context: Context, val resId: Int) : Sound(id) {
    val contextRef = WeakReference(context)
}

class FileSound(id: Int, val path: String) : Sound(id)

class AssetFileDescriptorSound(id: Int, val afd: AssetFileDescriptor) : Sound(id)

class FileDescriptorSound(
    id: Int,
    val fd: FileDescriptor,
    val offset: Long,
    val length: Long
) : Sound(id)