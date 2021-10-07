package com.yullg.android.scaffold.core

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.PowerManager
import com.yullg.android.scaffold.R
import com.yullg.android.scaffold.app.Scaffold
import com.yullg.android.scaffold.internal.ScaffoldLogger

/**
 * 通过播放音频实现应用保活和防止系统休眠（无限循环播放）
 *
 * 如果[onlyNotInteractive]设置为true（默认为true）：
 * 当[mount()]方法被调用后，开始监听设备的交互状态。如果设备是不可交互的，那么就开始播放音频，否则就停止播放音频。
 * 当[unmount()]方法被调用后，取消监听并停止播放音频。
 * 如果[onlyNotInteractive]设置为false：
 * 当[mount()]方法被调用后，立即开始播放音频。当[unmount()]方法被调用后，立即停止播放音频。不关心设备的交互状态。
 *
 * 如果[enhance]设置为true（默认为false）,那么会尝试在音频播放期间保持设备唤醒状态，开启此功能必须先获取[android.Manifest.permission.WAKE_LOCK]权限。
 */
class MusicKeepAlive(
    private val onlyNotInteractive: Boolean = true,
    private val enhance: Boolean = false
) {

    private val deviceInteractiveStateObserver = DeviceInteractiveStateObserver { isInteractive ->
        try {
            if (isInteractive) {
                unloadMediaPlayer()
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[MusicKeepAlive] Interactive state changed : Interactive = ON, Player = OFF")
                }
            } else {
                loadMediaPlayer()
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[MusicKeepAlive] Interactive state changed : Interactive = OFF, Player = ON")
                }
            }
        } catch (e: Exception) {
            if (ScaffoldLogger.isErrorEnabled()) {
                ScaffoldLogger.error("[MusicKeepAlive] Interactive state changed : Error", e)
            }
        }
    }
    private var mediaPlayer: MediaPlayer? = null

    fun mount() {
        try {
            if (onlyNotInteractive) {
                deviceInteractiveStateObserver.mount()
            } else {
                loadMediaPlayer()
            }
            if (ScaffoldLogger.isDebugEnabled()) {
                ScaffoldLogger.debug("[MusicKeepAlive] Mount succeeded")
            }
        } catch (e: Exception) {
            if (ScaffoldLogger.isErrorEnabled()) {
                ScaffoldLogger.error("[MusicKeepAlive] Mount failed", e)
            }
        }
    }

    fun unmount() {
        try {
            try {
                deviceInteractiveStateObserver.unmount()
            } finally {
                unloadMediaPlayer()
            }
            if (ScaffoldLogger.isDebugEnabled()) {
                ScaffoldLogger.debug("[MusicKeepAlive] Unmount succeeded")
            }
        } catch (e: Exception) {
            if (ScaffoldLogger.isErrorEnabled()) {
                ScaffoldLogger.error("[MusicKeepAlive] Unmount failed", e)
            }
        }
    }

    private fun loadMediaPlayer() {
        if (mediaPlayer != null) return
        val mp = MediaPlayer().apply {
            setAudioAttributes(AudioAttributes.Builder().run {
                setUsage(AudioAttributes.USAGE_MEDIA)
                setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                if (Build.VERSION.SDK_INT >= 29) {
                    setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_NONE)
                }
                build()
            })
            setOnErrorListener { _, what, extra ->
                if (ScaffoldLogger.isErrorEnabled()) {
                    ScaffoldLogger.error("[MusicKeepAlive] Player error : what = $what, extra = $extra")
                }
                false
            }
            setOnPreparedListener { mp ->
                try {
                    mp.start()
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[MusicKeepAlive] Player start succeeded")
                    }
                } catch (e: Exception) {
                    if (ScaffoldLogger.isErrorEnabled()) {
                        ScaffoldLogger.error("[MusicKeepAlive] Player start failed", e)
                    }
                }
            }
            if (enhance) {
                setWakeMode(Scaffold.context, PowerManager.PARTIAL_WAKE_LOCK)
            }
            setVolume(0F, 0F)
            isLooping = true
            Scaffold.context.resources.openRawResourceFd(R.raw.yg_music_keep_alive).use {
                setDataSource(it.fileDescriptor, it.startOffset, it.length)
            }
        }
        mp.prepareAsync()
        mediaPlayer = mp
    }

    private fun unloadMediaPlayer() {
        try {
            mediaPlayer?.release()
        } finally {
            mediaPlayer = null
        }
    }

}