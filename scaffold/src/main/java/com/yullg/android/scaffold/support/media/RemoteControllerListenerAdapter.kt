package com.yullg.android.scaffold.support.media

import android.media.*
import android.media.session.MediaController
import android.media.session.PlaybackState

/**
 * 提供从[RemoteController.OnClientUpdateListener]到[MediaController.Callback]的转换功能
 *
 * 在处理[onClientMetadataUpdate()]事件时丢弃了一部分不常用或者目标不支持的数据，已转发的数据包括：
 * 1. [MediaMetadataRetriever.METADATA_KEY_ALBUM]
 * 2. [MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST]
 * 3. [MediaMetadataRetriever.METADATA_KEY_ARTIST]
 * 4. [MediaMetadataRetriever.METADATA_KEY_AUTHOR]
 * 5. [MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER]
 * 6. [MediaMetadataRetriever.METADATA_KEY_COMPILATION]
 * 7. [MediaMetadataRetriever.METADATA_KEY_COMPOSER]
 * 8. [MediaMetadataRetriever.METADATA_KEY_DATE]
 * 9. [MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER]
 * 10. [MediaMetadataRetriever.METADATA_KEY_DURATION]
 * 11. [MediaMetadataRetriever.METADATA_KEY_GENRE]
 * 12. [MediaMetadataRetriever.METADATA_KEY_TITLE]
 * 13. [MediaMetadataRetriever.METADATA_KEY_WRITER]
 * 14. [MediaMetadataRetriever.METADATA_KEY_YEAR]
 * 15. [MediaMetadataEditor.BITMAP_KEY_ARTWORK]
 */
class RemoteControllerListenerAdapter(
    private val mediaControllerCallback: MediaController.Callback
) : RemoteController.OnClientUpdateListener {

    // 由于PlaybackState在RemoteController监听器中被拆分成两个部分分别触发，为了保证MediaController每次都能接收
    // 到完整的数据，此处将最后发送的数据缓存下来，每次发送数据前先和缓存数据合并。
    private var lastPlaybackState = PlaybackState.Builder().build()

    override fun onClientChange(clearing: Boolean) {
        mediaControllerCallback.onSessionDestroyed()
        // 清除前一个客户端的缓存数据
        lastPlaybackState = PlaybackState.Builder().build()
    }

    override fun onClientPlaybackStateUpdate(state: Int) {
        lastPlaybackState = PlaybackState.Builder(lastPlaybackState)
            .setState(convertState(state), PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1F)
            .build()
        mediaControllerCallback.onPlaybackStateChanged(lastPlaybackState)
    }

    override fun onClientPlaybackStateUpdate(
        state: Int,
        stateChangeTimeMs: Long,
        currentPosMs: Long,
        speed: Float
    ) {
        lastPlaybackState = PlaybackState.Builder(lastPlaybackState)
            .setState(convertState(state), currentPosMs, speed, stateChangeTimeMs)
            .build()
        mediaControllerCallback.onPlaybackStateChanged(lastPlaybackState)
    }

    override fun onClientTransportControlUpdate(transportControlFlags: Int) {
        var actions = 0L
        if (transportControlFlags or RemoteControlClient.FLAG_KEY_MEDIA_FAST_FORWARD == transportControlFlags) {
            actions = actions or PlaybackState.ACTION_FAST_FORWARD
        }
        if (transportControlFlags or RemoteControlClient.FLAG_KEY_MEDIA_NEXT == transportControlFlags) {
            actions = actions or PlaybackState.ACTION_SKIP_TO_NEXT
        }
        if (transportControlFlags or RemoteControlClient.FLAG_KEY_MEDIA_PAUSE == transportControlFlags) {
            actions = actions or PlaybackState.ACTION_PAUSE
        }
        if (transportControlFlags or RemoteControlClient.FLAG_KEY_MEDIA_PLAY == transportControlFlags) {
            actions = actions or PlaybackState.ACTION_PLAY
        }
        if (transportControlFlags or RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE == transportControlFlags) {
            actions = actions or PlaybackState.ACTION_PLAY_PAUSE
        }
        if (transportControlFlags or RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS == transportControlFlags) {
            actions = actions or PlaybackState.ACTION_SKIP_TO_PREVIOUS
        }
        if (transportControlFlags or RemoteControlClient.FLAG_KEY_MEDIA_RATING == transportControlFlags) {
            actions = actions or PlaybackState.ACTION_SET_RATING
        }
        if (transportControlFlags or RemoteControlClient.FLAG_KEY_MEDIA_REWIND == transportControlFlags) {
            actions = actions or PlaybackState.ACTION_REWIND
        }
        if (transportControlFlags or RemoteControlClient.FLAG_KEY_MEDIA_STOP == transportControlFlags) {
            actions = actions or PlaybackState.ACTION_STOP
        }
        lastPlaybackState = PlaybackState.Builder(lastPlaybackState)
            .setActions(actions)
            .build()
        mediaControllerCallback.onPlaybackStateChanged(lastPlaybackState)
    }

    override fun onClientMetadataUpdate(metadataEditor: RemoteController.MetadataEditor?) {
        val mediaMetadata: MediaMetadata? = metadataEditor?.run {
            val mediaMetadataBuilder = MediaMetadata.Builder()
            getString(MediaMetadataRetriever.METADATA_KEY_ALBUM, null)?.let {
                mediaMetadataBuilder.putString(MediaMetadata.METADATA_KEY_ALBUM, it)
            }
            getString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, null)?.let {
                mediaMetadataBuilder.putString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST, it)
            }
            getString(MediaMetadataRetriever.METADATA_KEY_ARTIST, null)?.let {
                mediaMetadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, it)
            }
            getString(MediaMetadataRetriever.METADATA_KEY_AUTHOR, null)?.let {
                mediaMetadataBuilder.putString(MediaMetadata.METADATA_KEY_AUTHOR, it)
            }
            getLong(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER, -1L).let {
                if (it >= 0) {
                    mediaMetadataBuilder.putLong(MediaMetadata.METADATA_KEY_TRACK_NUMBER, it)
                }
            }
            getString(MediaMetadataRetriever.METADATA_KEY_COMPILATION, null)?.let {
                mediaMetadataBuilder.putString(MediaMetadata.METADATA_KEY_COMPILATION, it)
            }
            getString(MediaMetadataRetriever.METADATA_KEY_COMPOSER, null)?.let {
                mediaMetadataBuilder.putString(MediaMetadata.METADATA_KEY_COMPOSER, it)
            }
            getString(MediaMetadataRetriever.METADATA_KEY_DATE, null)?.let {
                mediaMetadataBuilder.putString(MediaMetadata.METADATA_KEY_DATE, it)
            }
            getLong(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER, -1L).let {
                if (it >= 0) {
                    mediaMetadataBuilder.putLong(MediaMetadata.METADATA_KEY_DISC_NUMBER, it)
                }
            }
            getLong(MediaMetadataRetriever.METADATA_KEY_DURATION, -1L).let {
                if (it >= 0) {
                    mediaMetadataBuilder.putLong(MediaMetadata.METADATA_KEY_DURATION, it)
                }
            }
            getString(MediaMetadataRetriever.METADATA_KEY_GENRE, null)?.let {
                mediaMetadataBuilder.putString(MediaMetadata.METADATA_KEY_GENRE, it)
            }
            getString(MediaMetadataRetriever.METADATA_KEY_TITLE, null)?.let {
                mediaMetadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, it)
            }
            getString(MediaMetadataRetriever.METADATA_KEY_WRITER, null)?.let {
                mediaMetadataBuilder.putString(MediaMetadata.METADATA_KEY_WRITER, it)
            }
            getLong(MediaMetadataRetriever.METADATA_KEY_YEAR, -1L).let {
                if (it >= 0) {
                    mediaMetadataBuilder.putLong(MediaMetadata.METADATA_KEY_YEAR, it)
                }
            }
            getBitmap(MediaMetadataEditor.BITMAP_KEY_ARTWORK, null)?.let {
                mediaMetadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, it)
            }
            mediaMetadataBuilder.build()
        }
        mediaControllerCallback.onMetadataChanged(mediaMetadata)
    }

    private fun convertState(state: Int) = when (state) {
        RemoteControlClient.PLAYSTATE_BUFFERING -> PlaybackState.STATE_BUFFERING
        RemoteControlClient.PLAYSTATE_ERROR -> PlaybackState.STATE_ERROR
        RemoteControlClient.PLAYSTATE_FAST_FORWARDING -> PlaybackState.STATE_FAST_FORWARDING
        RemoteControlClient.PLAYSTATE_PAUSED -> PlaybackState.STATE_PAUSED
        RemoteControlClient.PLAYSTATE_PLAYING -> PlaybackState.STATE_PLAYING
        RemoteControlClient.PLAYSTATE_REWINDING -> PlaybackState.STATE_REWINDING
        RemoteControlClient.PLAYSTATE_SKIPPING_BACKWARDS -> PlaybackState.STATE_SKIPPING_TO_PREVIOUS
        RemoteControlClient.PLAYSTATE_SKIPPING_FORWARDS -> PlaybackState.STATE_SKIPPING_TO_NEXT
        RemoteControlClient.PLAYSTATE_STOPPED -> PlaybackState.STATE_STOPPED
        else -> PlaybackState.STATE_NONE
    }

}