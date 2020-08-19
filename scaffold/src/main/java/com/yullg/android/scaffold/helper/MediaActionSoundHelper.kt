package com.yullg.android.scaffold.helper

import android.media.MediaActionSound

object MediaActionSoundHelper {

    private val mediaActionSound: MediaActionSound by lazy {
        MediaActionSound()
    }

    fun playFocusComplete() = mediaActionSound.play(MediaActionSound.FOCUS_COMPLETE)

    fun playShutterClick() = mediaActionSound.play(MediaActionSound.SHUTTER_CLICK)

    fun playStartVideoRecording() = mediaActionSound.play(MediaActionSound.START_VIDEO_RECORDING)

    fun playStopVideoRecording() = mediaActionSound.play(MediaActionSound.STOP_VIDEO_RECORDING)

}