package com.yullg.android.scaffold.helper

import android.app.NotificationManager
import android.content.ComponentName
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.yullg.android.scaffold.app.Scaffold

/**
 * 提供通知相关的辅助功能
 */
object NotificationHelper {

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