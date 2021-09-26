package com.yullg.android.scaffold.support.media

import android.content.ComponentName
import android.media.AudioManager
import android.media.RemoteController
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.view.KeyEvent
import androidx.annotation.MainThread
import androidx.core.content.ContextCompat
import com.yullg.android.scaffold.app.Scaffold
import com.yullg.android.scaffold.internal.ScaffoldLogger
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 提供对音乐播放器的监听和控制
 *
 * 优先使用[MediaController]监听和控制音乐播放器，由于[MediaController]会在播放器暂停一段时间后自动关闭，
 * 为了避免[MediaController]关闭后功能失效，[MusicClient]会在这时挂载一个[RemoteController]接管操作，直到[MediaController]再次可用。
 */
@MainThread
class MusicClient(
    private val notificationListener: ComponentName,
    private val mediaControllerCallback: MediaController.Callback,
    private val remoteControllerCallback: RemoteController.OnClientUpdateListener,
    private val musicClientListener: MusicClientListener? = null
) {

    private val mediaSessionManager: MediaSessionManager by lazy {
        ContextCompat.getSystemService(Scaffold.context, MediaSessionManager::class.java)!!
    }
    private val audioManager: AudioManager by lazy {
        ContextCompat.getSystemService(Scaffold.context, AudioManager::class.java)!!
    }

    private val activeSessionsChangedListener = MyActiveSessionsChangedListener()
    private val mediaSessionMounted = AtomicBoolean(false)
    private val remoteControllerMounted = AtomicBoolean(false)

    private var mediaController: MediaController? = null
    private var remoteController: RemoteController? = null

    val handle: Handle = Handle()

    fun mount() {
        try {
            mountRemoteController()
            mountMediaSession()
            if (ScaffoldLogger.isInfoEnabled()) {
                ScaffoldLogger.info("[MusicClient] Mount succeeded")
            }
        } catch (e: Exception) {
            if (ScaffoldLogger.isErrorEnabled()) {
                ScaffoldLogger.error("[MusicClient] Mount failed", e)
            }
        }
    }

    fun unmount() {
        try {
            unmountMediaSession()
            unmountRemoteController()
            if (ScaffoldLogger.isInfoEnabled()) {
                ScaffoldLogger.info("[MusicClient] Unmount succeeded")
            }
        } catch (e: Exception) {
            if (ScaffoldLogger.isErrorEnabled()) {
                ScaffoldLogger.error("[MusicClient] Unmount failed", e)
            }
        }
    }

    private fun mountMediaSession() {
        if (mediaSessionMounted.compareAndSet(false, true)) {
            mediaSessionManager.addOnActiveSessionsChangedListener(
                activeSessionsChangedListener,
                notificationListener
            )
            musicClientListener?.onMediaSessionMounted()
            if (ScaffoldLogger.isDebugEnabled()) {
                ScaffoldLogger.debug("[MusicClient] MediaSession has been mounted")
            }
        }
    }

    private fun mountRemoteController() {
        if (remoteControllerMounted.compareAndSet(false, true)) {
            val localRemoteController = RemoteController(Scaffold.context, remoteControllerCallback)
            audioManager.registerRemoteController(localRemoteController)
            remoteController = localRemoteController
            // 将监听器触发放在remoteController赋值之后，保证从此刻开始MusicClient已处于完全挂载状态
            // 避免如果在监听器中使用MusicClient，而MusicClient未完成挂载操作可能出现的不一致问题
            musicClientListener?.onRemoteControllerMounted(localRemoteController)
            if (ScaffoldLogger.isDebugEnabled()) {
                ScaffoldLogger.debug("[MusicClient] RemoteController has been mounted")
            }
        }
    }

    private fun unmountMediaSession() {
        if (mediaSessionMounted.compareAndSet(true, false)) {
            mediaSessionManager.removeOnActiveSessionsChangedListener(activeSessionsChangedListener)
            musicClientListener?.onMediaSessionUnmounted()
            if (ScaffoldLogger.isDebugEnabled()) {
                ScaffoldLogger.debug("[MusicClient] MediaSession has been unmounted")
            }
        }
    }

    private fun unmountRemoteController() {
        if (remoteControllerMounted.compareAndSet(true, false)) {
            val localRemoteController = remoteController
            if (localRemoteController != null) {
                audioManager.unregisterRemoteController(localRemoteController)
            }
            remoteController = null
            if (localRemoteController != null) {
                // 将监听器触发放在remoteController赋值之后，保证从此刻开始MusicClient已处于完全卸载状态
                musicClientListener?.onRemoteControllerUnmounted(localRemoteController)
            }
            if (ScaffoldLogger.isDebugEnabled()) {
                ScaffoldLogger.debug("[MusicClient] RemoteController has been unmounted")
            }
        }
    }

    /**
     * 检查并更新当前优先级最高的媒体会话
     *
     * 如果存在可用的媒体会话，那么就停用[RemoteController]，否则启用[RemoteController]。
     *
     * 当同时存在多个媒体会话的情况下，[MediaSessionManager.OnActiveSessionsChangedListener]并不能准确反应出优先级的变化
     * （例如暂停其中一个播放器后播放另一个，监听器不会响应这种状态变化，但是[MediaSessionManager.getActiveSessions]却可以），
     * 所以目前通过每次都调用[MediaSessionManager.getActiveSessions]来获取并保存最后处于活动状态的媒体会话（即优先级最高）。
     */
    private fun checkMediaSession(): MediaController? = synchronized(this) {
        val currMediaController: MediaController? = mediaController
        val targetMediaController: MediaController? =
            mediaSessionManager.getActiveSessions(notificationListener).firstOrNull()
        if (currMediaController?.sessionToken != targetMediaController?.sessionToken) {
            currMediaController?.unregisterCallback(mediaControllerCallback)
            targetMediaController?.registerCallback(mediaControllerCallback)
            mediaController = targetMediaController
            currMediaController?.let {
                musicClientListener?.onMediaControllerDisconnected(it)
            }
            targetMediaController?.let {
                musicClientListener?.onMediaControllerConnected(it)
            }
            if (targetMediaController != null) {
                unmountRemoteController()
            } else {
                mountRemoteController()
            }
        }
        return mediaController
    }

    private inner class MyActiveSessionsChangedListener :
        MediaSessionManager.OnActiveSessionsChangedListener {

        override fun onActiveSessionsChanged(controllers: MutableList<MediaController>?) {
            try {
                checkMediaSession()
                if (ScaffoldLogger.isInfoEnabled()) {
                    ScaffoldLogger.info("[MusicClient] Active media session has been updated")
                }
            } catch (e: Exception) {
                if (ScaffoldLogger.isErrorEnabled()) {
                    ScaffoldLogger.error("[MusicClient] Failed to update active media session", e)
                }
            }
        }

    }

    inner class Handle {

        val mediaSessionManager: MediaSessionManager
            get() = this@MusicClient.mediaSessionManager

        val audioManager: AudioManager
            get() = this@MusicClient.audioManager

        val mediaController: MediaController?
            get() = this@MusicClient.checkMediaSession()

        val remoteController: RemoteController?
            get() = this@MusicClient.remoteController

    }

    companion object {

        /**
         * @see MediaController.TransportControls.play
         * @see AudioManager.dispatchMediaKeyEvent
         */
        fun play(handle: Handle) {
            try {
                handle.mediaController?.apply {
                    transportControls.play()
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[MusicClient] Play has been handled by MediaController")
                    }
                    return
                }
                dispatchMediaKeyEvent(handle, KeyEvent.KEYCODE_MEDIA_PLAY)
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[MusicClient] Play has been handled by AudioManager")
                }
            } catch (e: Exception) {
                if (ScaffoldLogger.isErrorEnabled()) {
                    ScaffoldLogger.error("[MusicClient] Play failed", e)
                }
            }
        }

        /**
         * @see MediaController.TransportControls.pause
         * @see AudioManager.dispatchMediaKeyEvent
         */
        fun pause(handle: Handle) {
            try {
                handle.mediaController?.apply {
                    transportControls.pause()
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[MusicClient] Pause has been handled by MediaController")
                    }
                    return
                }
                dispatchMediaKeyEvent(handle, KeyEvent.KEYCODE_MEDIA_PAUSE)
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[MusicClient] Pause has been handled by AudioManager")
                }
            } catch (e: Exception) {
                if (ScaffoldLogger.isErrorEnabled()) {
                    ScaffoldLogger.error("[MusicClient] Pause failed", e)
                }
            }
        }

        /**
         * @see MediaController.TransportControls.skipToNext
         * @see AudioManager.dispatchMediaKeyEvent
         */
        fun skipToNext(handle: Handle) {
            try {
                handle.mediaController?.apply {
                    transportControls.skipToNext()
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[MusicClient] SkipToNext has been handled by MediaController")
                    }
                    return
                }
                dispatchMediaKeyEvent(handle, KeyEvent.KEYCODE_MEDIA_NEXT)
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[MusicClient] SkipToNext has been handled by AudioManager")
                }
            } catch (e: Exception) {
                if (ScaffoldLogger.isErrorEnabled()) {
                    ScaffoldLogger.error("[MusicClient] SkipToNext failed", e)
                }
            }
        }

        /**
         * @see MediaController.TransportControls.skipToPrevious
         * @see AudioManager.dispatchMediaKeyEvent
         */
        fun skipToPrevious(handle: Handle) {
            try {
                handle.mediaController?.apply {
                    transportControls.skipToPrevious()
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[MusicClient] SkipToPrevious has been handled by MediaController")
                    }
                    return
                }
                dispatchMediaKeyEvent(handle, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[MusicClient] SkipToPrevious has been handled by AudioManager")
                }
            } catch (e: Exception) {
                if (ScaffoldLogger.isErrorEnabled()) {
                    ScaffoldLogger.error("[MusicClient] SkipToPrevious failed", e)
                }
            }
        }

        /**
         * @see MediaController.adjustVolume
         * @see AudioManager.adjustVolume
         */
        fun adjustVolume(handle: Handle, direction: Int, flags: Int) {
            try {
                handle.mediaController?.apply {
                    adjustVolume(direction, flags)
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[MusicClient] AdjustVolume has been handled by MediaController: direction = $direction, flags = $flags")
                    }
                    return
                }
                handle.audioManager.adjustVolume(direction, flags)
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[MusicClient] AdjustVolume has been handled by AudioManager: direction = $direction, flags = $flags")
                }
            } catch (e: Exception) {
                if (ScaffoldLogger.isErrorEnabled()) {
                    ScaffoldLogger.error(
                        "[MusicClient] AdjustVolume failed: direction = $direction, flags = $flags",
                        e
                    )
                }
            }
        }

        /**
         * @see MediaController.setVolumeTo
         * @see AudioManager.setStreamVolume
         */
        fun setVolumeTo(handle: Handle, value: Int, flags: Int) {
            try {
                handle.mediaController?.apply {
                    setVolumeTo(value, flags)
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[MusicClient] SetVolumeTo has been handled by MediaController: value = $value, flags = $flags")
                    }
                    return
                }
                handle.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, flags)
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[MusicClient] SetVolumeTo has been handled by AudioManager: value = $value, flags = $flags")
                }
            } catch (e: Exception) {
                if (ScaffoldLogger.isErrorEnabled()) {
                    ScaffoldLogger.error(
                        "[MusicClient] SetVolumeTo failed: value = $value, flags = $flags",
                        e
                    )
                }
            }
        }

        private fun dispatchMediaKeyEvent(handle: Handle, code: Int) {
            handle.audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, code))
            handle.audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, code))
        }

    }

}

interface MusicClientListener {

    fun onMediaSessionMounted() {}

    fun onMediaSessionUnmounted() {}

    fun onMediaControllerConnected(mediaController: MediaController) {}

    fun onMediaControllerDisconnected(mediaController: MediaController) {}

    fun onRemoteControllerMounted(remoteController: RemoteController) {}

    fun onRemoteControllerUnmounted(remoteController: RemoteController) {}

}