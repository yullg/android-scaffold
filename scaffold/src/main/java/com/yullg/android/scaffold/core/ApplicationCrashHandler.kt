package com.yullg.android.scaffold.core

import android.os.Build
import android.os.Process
import androidx.core.content.pm.PackageInfoCompat
import com.yullg.android.scaffold.helper.PackageHelper
import com.yullg.android.scaffold.helper.SystemHelper
import com.yullg.android.scaffold.internal.CrashLogger
import com.yullg.android.scaffold.internal.ScaffoldLogger
import com.yullg.android.scaffold.support.logger.Log
import com.yullg.android.scaffold.support.logger.LogLevel
import org.json.JSONObject
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 安装默认处理程序，在线程因未捕获的异常而突然终止且未为该线程定义其他处理程序时处理异常。
 *
 * 通过[install()]方法安装处理程序，处理程序依次执行以下操作：
 * 1.  使用[CrashLogger]记录运行环境和异常信息。可以通过[logging()]方法开启或关闭，默认为开启
 * 2.  将异常传递给原始的默认处理程序（如果有的话）。可以通过[withOriginalHandler()]方法开启或关闭，默认为开启
 * 3.  杀死当前进程。可以通过[kill()]方法开启或关闭，默认为开启
 *
 * 注意：[install()]方法只能被调用一次，超过一次的调用将被忽略。上述第1步操作和第3步操作最多执行一次，多次捕获异常将直接跳过，第2步操作不受此限制。
 */
object ApplicationCrashHandler {

    private val installed = AtomicBoolean(false)
    private var mLogging: Boolean = true
    private var mKill: Boolean = true
    private var mWithOriginalHandler: Boolean = true

    fun logging(value: Boolean): ApplicationCrashHandler {
        mLogging = value
        return this
    }

    fun kill(value: Boolean): ApplicationCrashHandler {
        mKill = value
        return this
    }

    fun withOriginalHandler(value: Boolean): ApplicationCrashHandler {
        mWithOriginalHandler = value
        return this
    }

    fun install() {
        if (installed.compareAndSet(false, true)) {
            val originalHandler: Thread.UncaughtExceptionHandler? = if (mWithOriginalHandler) {
                Thread.getDefaultUncaughtExceptionHandler()
            } else null
            Thread.setDefaultUncaughtExceptionHandler(
                DefaultUncaughtExceptionHandler(
                    mLogging = mLogging,
                    mKill = mKill,
                    mOriginalHandler = originalHandler
                )
            )
            if (ScaffoldLogger.isDebugEnabled()) {
                ScaffoldLogger.debug("[ApplicationCrashHandler] Install succeeded")
            }
        } else {
            if (ScaffoldLogger.isWarnEnabled()) {
                ScaffoldLogger.warn("[ApplicationCrashHandler] Install ignored")
            }
        }
    }

}

private class DefaultUncaughtExceptionHandler(
    val mLogging: Boolean,
    val mKill: Boolean,
    val mOriginalHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    private val triggered = AtomicBoolean(false)

    override fun uncaughtException(t: Thread, e: Throwable) {
        if (triggered.compareAndSet(false, true)) {
            try {
                if (mLogging) {
                    CrashLogger.log(
                        Log(
                            name = CrashLogger.name,
                            logLevel = LogLevel.FATAL,
                            message = "FATAL EXCEPTION >> ${generateMessage()}",
                            error = e,
                            threadId = t.id,
                            threadName = t.name
                        )
                    )
                }
            } finally {
                try {
                    mOriginalHandler?.uncaughtException(t, e)
                } finally {
                    if (mKill) {
                        Process.killProcess(Process.myPid())
                        System.exit(10)
                    }
                }
            }
        } else {
            // 自己仅执行一次，但不会限制原始处理器
            mOriginalHandler?.uncaughtException(t, e)
        }
    }

    private fun generateMessage(): String {
        val json = JSONObject()
        json.put("SSAID", SystemHelper.SSAID)
        json.put("LOCALE", Locale.getDefault().toString())
        json.put("TIME_ZONE", TimeZone.getDefault().id)
        json.put("Package", JSONObject().apply {
            put("packageName", PackageHelper.myPackageInfo.packageName)
            put("versionName", PackageHelper.myPackageInfo.versionName)
            put(
                "versionCode",
                PackageInfoCompat.getLongVersionCode(PackageHelper.myPackageInfo)
            )
            put("firstInstallTime", PackageHelper.myPackageInfo.firstInstallTime)
            put("lastUpdateTime", PackageHelper.myPackageInfo.lastUpdateTime)
        })
        json.put("Build", JSONObject().apply {
            put("BOARD", Build.BOARD)
            put("BOOTLOADER", Build.BOOTLOADER)
            put("BRAND", Build.BRAND)
            put("DEVICE", Build.DEVICE)
            put("DISPLAY", Build.DISPLAY)
            put("FINGERPRINT", Build.FINGERPRINT)
            put("HARDWARE", Build.HARDWARE)
            put("HOST", Build.HOST)
            put("ID", Build.ID)
            put("MANUFACTURER", Build.MANUFACTURER)
            put("MODEL", Build.MODEL)
            put("PRODUCT", Build.PRODUCT)
            put("SUPPORTED_ABIS", Arrays.toString(Build.SUPPORTED_ABIS))
            put("TAGS", Build.TAGS)
            put("TIME", Build.TIME)
            put("TYPE", Build.TYPE)
            put("USER", Build.USER)
            put("RADIO", Build.getRadioVersion())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                put("ODM_SKU", Build.ODM_SKU)
                put("SKU", Build.SKU)
                put("SOC_MANUFACTURER", Build.SOC_MANUFACTURER)
                put("SOC_MODEL", Build.SOC_MODEL)
            }
            put("VERSION_RELEASE", Build.VERSION.RELEASE)
            put("VERSION_SDK_INT", Build.VERSION.SDK_INT)
        })
        return json.toString()
    }

}