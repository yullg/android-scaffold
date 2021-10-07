package com.yullg.android.scaffold.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.yullg.android.scaffold.app.Scaffold
import com.yullg.android.scaffold.internal.ScaffoldLogger

/**
 * 监听设备的交互状态
 *
 * 当[mount()]方法被调用后，开始监听设备的交互状态。如果设备是可交互的，那么就调用`consumer(true)`，否则调用`consumer(false)`。
 * 如果[consumeCurrentState]设置为`true`（默认为true），那么在调用[mount()]时立即检查交互状态并触发[consumer]。
 *
 * [consumer]总是在`Main`线程被调用。
 */
class DeviceInteractiveStateObserver(
    private val consumeCurrentState: Boolean = true,
    private val consumer: (Boolean) -> Unit
) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val screenBroadcastReceiver = ScreenBroadcastReceiver()
    private var mounted: Boolean = false

    fun mount() = synchronized(this) {
        if (mounted) return@synchronized
        mounted = true
        try {
            if (consumeCurrentState) {
                ContextCompat.getSystemService(Scaffold.context, PowerManager::class.java)
                    ?.isInteractive?.let { isInteractive ->
                        mainHandler.post {
                            consumer(isInteractive)
                        }
                    }
            }
        } finally {
            Scaffold.context.registerReceiver(screenBroadcastReceiver, IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            })
        }
    }

    fun unmount() = synchronized(this) {
        if (!mounted) return@synchronized
        try {
            Scaffold.context.unregisterReceiver(screenBroadcastReceiver)
        } finally {
            mounted = false
        }
    }

    /**
     * 交互状态广播接收器
     */
    private inner class ScreenBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null || intent == null) return
            if (Intent.ACTION_SCREEN_ON == intent.action) {
                consumer(true)
            } else if (Intent.ACTION_SCREEN_OFF == intent.action) {
                consumer(false)
            } else {
                if (ScaffoldLogger.isWarnEnabled()) {
                    ScaffoldLogger.warn("[DeviceInteractiveStateObserver] ScreenBroadcastReceiver onReceive : Illegal Action = ${intent.action}")
                }
            }
        }

    }

}