package com.yullg.android.scaffold.core

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.PowerManager
import android.view.Gravity
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.yullg.android.scaffold.app.Scaffold
import com.yullg.android.scaffold.internal.ScaffoldLogger
import java.lang.ref.WeakReference

@MainThread
class OnePixelKeepAlive {

    private var screenBroadcastReceiver: ScreenBroadcastReceiver? = null

    fun start() {
        try {
            startScreenScheduler()
            ContextCompat.getSystemService(Scaffold.context, PowerManager::class.java)
                ?.isInteractive?.let {
                    if (it) {
                        if (ScaffoldLogger.isDebugEnabled()) {
                            ScaffoldLogger.debug("[OnePixelKeepAlive] OPA load delay until the screen off")
                        }
                    } else {
                        loadOnePixelActivity()
                        if (ScaffoldLogger.isDebugEnabled()) {
                            ScaffoldLogger.debug("[OnePixelKeepAlive] OPA loaded")
                        }
                    }
                }
            if (ScaffoldLogger.isDebugEnabled()) {
                ScaffoldLogger.debug("[OnePixelKeepAlive] Start succeeded")
            }
        } catch (e: Exception) {
            if (ScaffoldLogger.isErrorEnabled()) {
                ScaffoldLogger.error("[OnePixelKeepAlive] Start failed", e)
            }
        }
    }

    fun stop() {
        try {
            try {
                stopScreenScheduler()
            } finally {
                unloadOnePixelActivity()
            }
            if (ScaffoldLogger.isDebugEnabled()) {
                ScaffoldLogger.debug("[OnePixelKeepAlive] Stop succeeded")
            }
        } catch (e: Exception) {
            if (ScaffoldLogger.isErrorEnabled()) {
                ScaffoldLogger.error("[OnePixelKeepAlive] Stop failed", e)
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

    private inner class ScreenBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                if (context == null || intent == null) return
                if (Intent.ACTION_SCREEN_ON == intent.action) {
                    unloadOnePixelActivity()
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[OnePixelKeepAlive] SBR schedule : Screen = ON : OPA = OFF")
                    }
                } else if (Intent.ACTION_SCREEN_OFF == intent.action) {
                    loadOnePixelActivity()
                    if (ScaffoldLogger.isDebugEnabled()) {
                        ScaffoldLogger.debug("[OnePixelKeepAlive] SBR schedule : Screen = OFF : OPA = ON")
                    }
                } else {
                    if (ScaffoldLogger.isWarnEnabled()) {
                        ScaffoldLogger.warn("[OnePixelKeepAlive] SBR schedule : Illegal action : ${intent.action}")
                    }
                }
            } catch (e: Exception) {
                if (ScaffoldLogger.isErrorEnabled()) {
                    ScaffoldLogger.error("[OnePixelKeepAlive] SBR catch error", e)
                }
            }
        }

    }

    internal companion object {

        var activityRef: WeakReference<Activity>? = null

        fun loadOnePixelActivity() {
            unloadOnePixelActivity()
            Scaffold.context.startActivity(
                Intent(Scaffold.context, OnePixelKeepAliveActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
        }

        fun unloadOnePixelActivity() {
            if (activityRef?.get() == null) return
            activityRef?.get()?.finish()
            activityRef?.clear()
            activityRef = null
        }

    }

}

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