package com.yullg.android.scaffold.support.media

import android.media.MediaActionSound

/**
 * 一个[MediaActionSound]实例，提供便捷的方式播放与相机的各种操作相匹配的声音。
 */
object MediaActionSoundPlayer {

    private val mediaActionSound: MediaActionSound by lazy {
        MediaActionSound()
    }

    fun playFocusComplete() = mediaActionSound.play(MediaActionSound.FOCUS_COMPLETE)

    fun playShutterClick() = mediaActionSound.play(MediaActionSound.SHUTTER_CLICK)

    fun playStartVideoRecording() = mediaActionSound.play(MediaActionSound.START_VIDEO_RECORDING)

    fun playStopVideoRecording() = mediaActionSound.play(MediaActionSound.STOP_VIDEO_RECORDING)

}