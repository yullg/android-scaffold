package com.yullg.android.scaffold.core

import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import java.util.concurrent.atomic.AtomicLong

/**
 * 启动一个Activity并接收它的结果
 *
 * 相比于直接使用AndroidX提供的[`Activity Result APIs`](https://developer.android.com/training/basics/intents/result)，
 * 此类将结果回调[ActivityResultCallback]延迟到实际启动时才需要，这种交互对编码更加友好，但是如果过程中调用方进程或`Activity`栈被系统销毁，
 * 目标Activity的结果将无法接收。例如：你的应用启动一个相机应用，并接收捕获的照片作为结果，在此过程中如果你的应用进程或`Activity`栈由于低内存
 * 等原因被系统销毁，当拍照结束后返回到你的应用，你的应用进程和`Activity`被重建，但是拍照结果将无法再接收。
 */
class ResultActivityLauncher<I : Any?, O : Any?>(
    caller: ActivityResultCaller,
    contract: ActivityResultContract<I, O>
) {

    private val launchId = AtomicLong()

    private var callbackWrapper: ActivityResultCallbackWrapper<O>? = null

    private val launcher = caller.registerForActivityResult(contract) { result ->
        callbackWrapper?.let { wrapper ->
            // 仅处理最后一次调用launch时传递的callback
            if (launchId.get() == wrapper.callbackId) {
                try {
                    wrapper.callback.onActivityResult(result)
                } finally {
                    // 每个callback应该最多被使用一次
                    callbackWrapper = null
                }
            }
        }
    }

    fun launch(input: I, callback: ActivityResultCallback<O>) {
        this.callbackWrapper = ActivityResultCallbackWrapper(
            launchId.incrementAndGet(),
            callback
        )
        launcher.launch(input)
    }

    fun launch(input: I, options: ActivityOptionsCompat?, callback: ActivityResultCallback<O>) {
        this.callbackWrapper = ActivityResultCallbackWrapper(
            launchId.incrementAndGet(),
            callback
        )
        launcher.launch(input, options)
    }

    private class ActivityResultCallbackWrapper<T>(
        val callbackId: Long,
        val callback: ActivityResultCallback<T>
    )

}