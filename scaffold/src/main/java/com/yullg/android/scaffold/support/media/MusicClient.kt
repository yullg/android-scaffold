package com.yullg.android.scaffold.support.media

import android.content.ComponentName
import android.media.AudioManager
import android.media.RemoteController
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import com.yullg.android.scaffold.app.Scaffold
import com.yullg.android.scaffold.internal.ScaffoldLogger
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 提供对音乐播放器的监听和控制
 *
 * 该类主要负责在[MediaController]和[RemoteController]之间自动切换，通过各自的`Callback`通知音乐状态的变化，
 * 通过[handle]属性暴露内部状态用以实现音乐控制，伴生对象基于[handle]提供播放、暂停、上一首、下一首、音量调节等部分常用功能。
 *
 * Android平台目前监听第三方音乐播放器可以基于[MediaController]实现，也可以基于[RemoteController]实现，后一种方式从API 21开始被弃用。
 * [MediaController]比[RemoteController]提供更完整的功能，例如：歌词、音量监听等，但是[MediaController]会在媒体会话暂停一段时间后断开连接，
 * 这将导致[MediaController]功能不可用，并且也无法保证[MediaController]一定能连接（或再次连接）到媒体会话，因此需要在[MediaController]无法使用时切换到[RemoteController]。
 *
 * 注意：[RemoteController]要求其注册的状态回调(即[remoteControllerCallback])也必须是已启用的通知监听器(参见[android.service.notification.NotificationListenerService])。
 */
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
            ScaffoldLogger.info("[MusicClient] Mount succeeded")
        } catch (e: Exception) {
            ScaffoldLogger.error("[MusicClient] Mount failed", e)
        }
    }

    fun unmount() {
        try {
            unmountMediaSession()
            unmountRemoteController()
            ScaffoldLogger.info("[MusicClient] Unmount succeeded")
        } catch (e: Exception) {
            ScaffoldLogger.error("[MusicClient] Unmount failed", e)
        }
    }

    private fun mountMediaSession() {
        if (mediaSessionMounted.compareAndSet(false, true)) {
            mediaSessionManager.addOnActiveSessionsChangedListener(
                activeSessionsChangedListener,
                notificationListener
            )
            musicClientListener?.onMediaSessionMounted()
            ScaffoldLogger.debug("[MusicClient] MediaSession has been mounted")
        }
    }

    private fun mountRemoteController() {
        if (remoteControllerMounted.compareAndSet(false, true)) {
            val localRemoteController = RemoteController(Scaffold.context, remoteControllerCallback)
            audioManager.registerRemoteController(localRemoteController)
            remoteController = localRemoteController
            // 将监听器触发放在remoteController赋值之后，保证从此刻开始MusicClient已处于完全挂载状态
            // 避免如果在监听器中使用MusicClient，而MusicClient还未结束挂载操作时可能出现的不一致问题
            musicClientListener?.onRemoteControllerMounted(localRemoteController)
            ScaffoldLogger.debug("[MusicClient] RemoteController has been mounted")
        }
    }

    private fun unmountMediaSession() {
        if (mediaSessionMounted.compareAndSet(true, false)) {
            mediaSessionManager.removeOnActiveSessionsChangedListener(activeSessionsChangedListener)
            musicClientListener?.onMediaSessionUnmounted()
            ScaffoldLogger.debug("[MusicClient] MediaSession has been unmounted")
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
            ScaffoldLogger.debug("[MusicClient] RemoteController has been unmounted")
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
                ScaffoldLogger.info("[MusicClient] Active media session has been updated")
            } catch (e: Exception) {
                ScaffoldLogger.error("[MusicClient] Failed to update active media session", e)
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

        fun play(handle: Handle) {
            try {
                handle.mediaController?.apply {
                    transportControls.play()
                    ScaffoldLogger.debug("[MusicClient] Play has been handled by MediaController")
                    return
                }
                dispatchMediaKeyEvent(handle, KeyEvent.KEYCODE_MEDIA_PLAY)
                ScaffoldLogger.debug("[MusicClient] Play has been handled by AudioManager")
            } catch (e: Exception) {
                ScaffoldLogger.error("[MusicClient] Play failed", e)
            }
        }

        fun pause(handle: Handle) {
            try {
                handle.mediaController?.apply {
                    transportControls.pause()
                    ScaffoldLogger.debug("[MusicClient] Pause has been handled by MediaController")
                    return
                }
                dispatchMediaKeyEvent(handle, KeyEvent.KEYCODE_MEDIA_PAUSE)
                ScaffoldLogger.debug("[MusicClient] Pause has been handled by AudioManager")
            } catch (e: Exception) {
                ScaffoldLogger.error("[MusicClient] Pause failed", e)
            }
        }

        fun skipToNext(handle: Handle) {
            try {
                handle.mediaController?.apply {
                    transportControls.skipToNext()
                    ScaffoldLogger.debug("[MusicClient] SkipToNext has been handled by MediaController")
                    return
                }
                dispatchMediaKeyEvent(handle, KeyEvent.KEYCODE_MEDIA_NEXT)
                ScaffoldLogger.debug("[MusicClient] SkipToNext has been handled by AudioManager")
            } catch (e: Exception) {
                ScaffoldLogger.error("[MusicClient] SkipToNext failed", e)
            }
        }

        fun skipToPrevious(handle: Handle) {
            try {
                handle.mediaController?.apply {
                    transportControls.skipToPrevious()
                    ScaffoldLogger.debug("[MusicClient] SkipToPrevious has been handled by MediaController")
                    return
                }
                dispatchMediaKeyEvent(handle, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                ScaffoldLogger.debug("[MusicClient] SkipToPrevious has been handled by AudioManager")
            } catch (e: Exception) {
                ScaffoldLogger.error("[MusicClient] SkipToPrevious failed", e)
            }
        }

        fun adjustVolume(handle: Handle, direction: Int, flags: Int) {
            try {
                handle.mediaController?.apply {
                    adjustVolume(direction, flags)
                    ScaffoldLogger.debug("[MusicClient] AdjustVolume has been handled by MediaController: direction = $direction, flags = $flags")
                    return
                }
                handle.audioManager.adjustVolume(direction, flags)
                ScaffoldLogger.debug("[MusicClient] AdjustVolume has been handled by AudioManager: direction = $direction, flags = $flags")
            } catch (e: Exception) {
                ScaffoldLogger.error(
                    "[MusicClient] AdjustVolume failed: direction = $direction, flags = $flags",
                    e
                )
            }
        }

        fun setVolumeTo(handle: Handle, value: Int, flags: Int) {
            try {
                handle.mediaController?.apply {
                    setVolumeTo(value, flags)
                    ScaffoldLogger.debug("[MusicClient] SetVolumeTo has been handled by MediaController: value = $value, flags = $flags")
                    return
                }
                handle.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, flags)
                ScaffoldLogger.debug("[MusicClient] SetVolumeTo has been handled by AudioManager: value = $value, flags = $flags")
            } catch (e: Exception) {
                ScaffoldLogger.error(
                    "[MusicClient] SetVolumeTo failed: value = $value, flags = $flags",
                    e
                )
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