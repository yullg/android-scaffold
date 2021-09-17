package com.yullg.android.scaffold.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.PowerManager
import androidx.annotation.MainThread
import androidx.core.content.ContextCompat
import com.yullg.android.scaffold.R
import com.yullg.android.scaffold.app.Scaffold
import com.yullg.android.scaffold.internal.ScaffoldLogger

@MainThread
class MusicKeepAlive(
    private val onlyScreenOff: Boolean = true,
    private val enhance: Boolean = false
) {

    private var screenBroadcastReceiver: ScreenBroadcastReceiver? = null
    private var mediaPlayer: MediaPlayer? = null

    fun start() {
        try {
            if (onlyScreenOff) {
                startScreenScheduler()
                ContextCompat.getSystemService(Scaffold.context, PowerManager::class.java)
                    ?.isInteractive?.let {
                        if (it) {
                            if (ScaffoldLogger.isDebugEnabled()) {
                                ScaffoldLogger.debug("[MusicKeepAlive] Player load delay until the screen off")
                            }
                        } else {
                            loadMediaPlayer()
                            if (ScaffoldLogger.isDebugEnabled()) {
                                ScaffoldLogger.debug("[MusicKeepAlive] Player loaded")
                            }
                        }
                    }
            } else {
                loadMediaPlayer()
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[MusicKeepAlive] Player loaded")
                }
            }
            if (ScaffoldLogger.isDebugEnabled()) {
                ScaffoldLogger.debug("[MusicKeepAlive] Start succeeded")
            }
        } catch (e: Exception) {
            if (ScaffoldLogger.isErrorEnabled()) {
                ScaffoldLogger.error("[MusicKeepAlive] Start failed", e)
            }
        }
    }

    fun stop() {
        try {
            try {
                stopScreenScheduler()
            } finally {
                unloadMediaPlayer()
            }
            if (ScaffoldLogger.isDebugEnabled()) {
                ScaffoldLogger.debug("[MusicKeepAlive] Stop succeeded")
            }
        } catch (e: Exception) {
            if (ScaffoldLogger.isErrorEnabled()) {
                ScaffoldLogger.error("[MusicKeepAlive] Stop failed", e)
            }
        }
    }

    private fun startScreenScheduler() {
        if (screenBroadcastReceiver != null) return
        screenBroadcastReceiver = ScreenBroadcastReceiver().also {
            Scaffold.context.registerReceiver(it, IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            })
        }
    }

    private fun stopScreenScheduler() {
        if (screenBroadcastReceiver == null) return
        try {
            screenBroadcastReceiver?.let {
                Scaffold.context.unregisterReceiver(it)
            }
        } finally {
            screenBroadcastReceiver = null
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
                        ScaffoldLogger.debug("[MusicKeepAlive] Player play succeeded")
                    }
                } catch (e: Exception) {
                    if (ScaffoldLogger.isErrorEnabled()) {
                        ScaffoldLogger.error("[MusicKeepAlive] Player play failed", e)
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
        if (mediaPlayer == null) return
        try {
            mediaPlayer?.release()
        } finally {
            mediaPlayer = null
        }
    }

    private inner class ScreenBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                if (context == null || intent == null) return
                if (Intent.ACTION_SCREEN_ON == intent.action) {
                    unloadMediaPlayer()
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[MusicKeepAlive] SBR schedule : Screen = ON : MP = OFF")
                    }
                } else if (Intent.ACTION_SCREEN_OFF == intent.action) {
                    loadMediaPlayer()
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[MusicKeepAlive] SBR schedule : Screen = OFF : MP = ON")
                    }
                } else {
                    if (ScaffoldLogger.isWarnEnabled()) {
                        ScaffoldLogger.warn("[MusicKeepAlive] SBR schedule : Illegal action : ${intent.action}")
                    }
                }
            } catch (e: Exception) {
                if (ScaffoldLogger.isErrorEnabled()) {
                    ScaffoldLogger.error("[MusicKeepAlive] SBR catch error", e)
                }
            }
        }

    }

}