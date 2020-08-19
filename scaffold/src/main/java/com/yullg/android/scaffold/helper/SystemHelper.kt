package com.yullg.android.scaffold.helper

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.yullg.android.scaffold.app.Scaffold
import com.yullg.android.scaffold.internal.ScaffoldLogger

object SystemHelper {

    val SSAID: String by lazy {
        Settings.Secure.getString(Scaffold.context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun hasSystemFeature(featureName: String): Boolean =
        Scaffold.context.packageManager.hasSystemFeature(featureName)

    fun startSettingsActivity(action: String, custom: ((Intent) -> Intent)? = null): Boolean {
        try {
            var settingsIntent = Intent(action).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            }
            if (custom != null) {
                settingsIntent = custom(settingsIntent)
            }
            Scaffold.context.startActivity(settingsIntent)
            return true
        } catch (e: Exception) {
            ScaffoldLogger.error("Unable to start 'Settings Activity' [ $action ]", e)
            return false
        }
    }

    val CUSTOM_SET_DATA_MY_APPLICATION_URI: (Intent) -> Intent = {
        it.setData(Uri.fromParts("package", Scaffold.context.packageName, null))
    }

}