package com.yullg.android.scaffold.helper

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import com.yullg.android.scaffold.app.Scaffold
import com.yullg.android.scaffold.internal.ScaffoldLogger

/**
 * 提供系统设置相关的辅助功能
 */
object SettingsHelper {

    val SSAID: String by lazy {
        Settings.Secure.getString(Scaffold.context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    /**
     * 打开设置通知监听器权限的`Activity`。
     * 如果Android平台版本大于或等于30并且提供了[listener]，那么将定位到具体的组件。
     *
     * @see Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS
     * @see Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
     */
    fun startNotificationListenerSettingsActivity(listener: ComponentName? = null): Boolean {
        return if (Build.VERSION.SDK_INT >= 30 && listener != null) {
            return startSettingsActivity(
                action = Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS,
                extras = Bundle().apply {
                    putString(
                        Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                        listener.flattenToString()
                    )
                }
            )
        } else if (Build.VERSION.SDK_INT >= 22) {
            return startSettingsActivity(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        } else {
            // 由于某些原因，常量字段从API Level 22开始才可用，所以低版本需要直接使用字符串
            // https://issuetracker.google.com/issues/36976015
            return startSettingsActivity("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        }
    }

    /**
     * 打开指定的应用程序信息设置Activity
     *
     * @see Settings.ACTION_APPLICATION_DETAILS_SETTINGS
     */
    fun startApplicationDetailsSettingsActivity(packageName: String): Boolean =
        startSettingsActivity(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:$packageName")
        )

    /**
     * 打开系统设置Activity
     */
    fun startSettingsActivity(action: String, uri: Uri? = null, extras: Bundle? = null): Boolean =
        try {
            val intent = if (uri == null) {
                Intent(action)
            } else {
                Intent(action, uri)
            }.apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                extras?.let { putExtras(it) }
            }
            Scaffold.context.startActivity(intent)
            true
        } catch (e: Exception) {
            ScaffoldLogger.warn(
                "[SettingsHelper] Unable to start Settings-Activity: action = $action, uri = $uri, extras = $extras",
                e
            )
            false
        }

}