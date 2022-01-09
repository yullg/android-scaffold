package com.yullg.android.scaffold.helper

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
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
     * 获取一个[Intent]，可以用来启动关于特定应用程序设置的`Activity`。
     *
     * @see Settings.ACTION_APPLICATION_DETAILS_SETTINGS
     */
    fun newIntentForApplicationDetailsSettings(packageName: String): Intent {
        return newIntentBySettingsAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
    }

    /**
     * 获取一个[Intent]，可以用来启动设置通知监听器权限的`Activity`。
     * 如果Android平台版本大于或等于30并且提供了[listener]，那么将定位到具体的组件。
     *
     * @see Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS
     * @see Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
     */
    fun newIntentForNotificationListenerSettings(listener: ComponentName? = null): Intent {
        return if (Build.VERSION.SDK_INT >= 30 && listener != null) {
            newIntentBySettingsAction(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).apply {
                putExtra(
                    Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                    listener.flattenToString()
                )
            }
        } else if (Build.VERSION.SDK_INT >= 22) {
            newIntentBySettingsAction(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        } else {
            // 由于某些原因，常量字段从API Level 22开始才可用，所以低版本需要直接使用字符串
            // https://issuetracker.google.com/issues/36976015
            newIntentBySettingsAction("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        }
    }

    /**
     * 通过给定的[action]创建适用于从应用内打开系统设置这一场景的[Intent]
     */
    fun newIntentBySettingsAction(action: String) = Intent(action).apply {
        addCategory(Intent.CATEGORY_DEFAULT)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }

    /**
     * 打开系统设置Activity，捕获所有异常。
     */
    fun startSettingsActivity(intent: Intent): Boolean = try {
        Scaffold.context.startActivity(intent)
        true
    } catch (e: Exception) {
        ScaffoldLogger.warn(
            "[SettingsHelper] Unable to start 'Settings-Activity': action = ${intent.action}",
            e
        )
        false
    }

}