package com.yullg.android.scaffold.helper

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.yullg.android.scaffold.app.Scaffold

/**
 * 提供通知功能相关的助手方法
 */
object NotificationHelper {

    /**
     * 获取一个[Intent]，可以用来启动设置通知监听器权限的`Activity`。
     * 如果Android平台版本大于或等于30并且提供了[listener]，那么将定位到具体的组件。
     */
    fun getIntentForNotificationListenerSettings(listener: ComponentName? = null): Intent {
        return if (Build.VERSION.SDK_INT >= 30 && listener != null) {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                putExtra(
                    Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                    listener.flattenToString()
                )
            }
        } else if (Build.VERSION.SDK_INT >= 22) {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            }
        } else {
            // 由于某些原因，常量字段从API Level 22开始才可用，所以低版本需要直接使用字符串
            // https://issuetracker.google.com/issues/36976015
            Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            }
        }
    }

    /**
     * 检查用户是否批准了给定的`NotificationListenerService`，
     * 如果Android平台版本小于27或者没有提供[listener]，那么仅检查当前应用是否具有任意已启用的`NotificationListenerService`。
     */
    fun isNotificationListenerAccessGranted(listener: ComponentName? = null): Boolean {
        val context = Scaffold.context
        if (Build.VERSION.SDK_INT >= 27 && listener != null) {
            ContextCompat.getSystemService(context, NotificationManager::class.java)?.apply {
                return isNotificationListenerAccessGranted(listener)
            }
        }
        NotificationManagerCompat.getEnabledListenerPackages(context).apply {
            return contains(context.packageName)
        }
    }

}