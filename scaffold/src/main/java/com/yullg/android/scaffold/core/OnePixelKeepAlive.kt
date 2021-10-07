package com.yullg.android.scaffold.core

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import com.yullg.android.scaffold.app.Scaffold
import com.yullg.android.scaffold.internal.ScaffoldLogger
import java.lang.ref.WeakReference

/**
 * 通过一像素Activity实现应用保活
 *
 * 当[mount()]方法被调用后，开始监听设备交互状态。如果设备是不可交互的，那么就启动[OnePixelKeepAliveActivity]，否则就关闭[OnePixelKeepAliveActivity]。
 * 当[unmount()]方法被调用后，取消监听并关闭[OnePixelKeepAliveActivity]。
 */
class OnePixelKeepAlive {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val deviceInteractiveStateObserver = DeviceInteractiveStateObserver { isInteractive ->
        try {
            if (isInteractive) {
                unloadOnePixelActivity()
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[OnePixelKeepAlive] Interactive state changed : Interactive = ON, Activity = OFF")
                }
            } else {
                loadOnePixelActivity()
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("[OnePixelKeepAlive] Interactive state changed : Interactive = OFF, Activity = ON")
                }
            }
        } catch (e: Exception) {
            if (ScaffoldLogger.isErrorEnabled()) {
                ScaffoldLogger.error("[OnePixelKeepAlive] Interactive state changed : Error", e)
            }
        }
    }

    fun mount() {
        try {
            deviceInteractiveStateObserver.mount()
            if (ScaffoldLogger.isDebugEnabled()) {
                ScaffoldLogger.debug("[OnePixelKeepAlive] Mount succeeded")
            }
        } catch (e: Exception) {
            if (ScaffoldLogger.isErrorEnabled()) {
                ScaffoldLogger.error("[OnePixelKeepAlive] Mount failed", e)
            }
        }
    }

    fun unmount() {
        try {
            try {
                deviceInteractiveStateObserver.unmount()
            } finally {
                mainHandler.post {
                    try {
                        unloadOnePixelActivity()
                    } catch (e: Exception) {
                        if (ScaffoldLogger.isErrorEnabled()) {
                            ScaffoldLogger.error(
                                "[OnePixelKeepAlive] Error unloading activity during unmount",
                                e
                            )
                        }
                    }
                }
            }
            if (ScaffoldLogger.isDebugEnabled()) {
                ScaffoldLogger.debug("[OnePixelKeepAlive] Unmount succeeded")
            }
        } catch (e: Exception) {
            if (ScaffoldLogger.isErrorEnabled()) {
                ScaffoldLogger.error("[OnePixelKeepAlive] Unmount failed", e)
            }
        }
    }

    /**
     * 伴生对象负责启动和关闭[OnePixelKeepAliveActivity]，持有已启动的`Activity`实例的弱引用。
     */
    internal companion object {

        var activityRef: WeakReference<Activity>? = null

        @MainThread
        fun loadOnePixelActivity() {
            unloadOnePixelActivity()
            Scaffold.context.startActivity(
                Intent(Scaffold.context, OnePixelKeepAliveActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
        }

        @MainThread
        fun unloadOnePixelActivity() {
            if (activityRef?.get() == null) return
            activityRef?.get()?.finish()
            activityRef?.clear()
            activityRef = null
        }

    }

}

/**
 * 一像素`Activity`，启动后占据屏幕左上角一像素空间。
 */
class OnePixelKeepAliveActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            OnePixelKeepAlive.activityRef = WeakReference(this)
            window.setGravity(Gravity.START or Gravity.TOP)
            window.attributes = window.attributes.apply {
                x = 0
                y = 0
                width = 1
                height = 1
            }
            if (ScaffoldLogger.isDebugEnabled()) {
                ScaffoldLogger.debug("[OnePixelKeepAlive] Activity create succeeded")
            }
        } catch (e: Exception) {
            if (ScaffoldLogger.isErrorEnabled()) {
                ScaffoldLogger.error("[OnePixelKeepAlive] Activity create failed", e)
            }
        }
    }

    override fun onDestroy() {
        try {
            OnePixelKeepAlive.activityRef = null
            if (ScaffoldLogger.isDebugEnabled()) {
                ScaffoldLogger.debug("[OnePixelKeepAlive] Activity destroy succeeded")
            }
        } catch (e: Exception) {
            if (ScaffoldLogger.isErrorEnabled()) {
                ScaffoldLogger.error("[OnePixelKeepAlive] Activity destroy failed", e)
            }
        }
        super.onDestroy()
    }

}