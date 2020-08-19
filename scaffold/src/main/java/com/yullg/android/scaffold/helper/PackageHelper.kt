package com.yullg.android.scaffold.helper

import android.content.Intent
import android.content.pm.PackageInfo
import com.yullg.android.scaffold.app.Scaffold

object PackageHelper {

    val myPackageInfo: PackageInfo
        get() = with(Scaffold.context) {
            packageManager.getPackageInfo(packageName, 0)
        }

    val myLaunchIntent: Intent?
        get() = with(Scaffold.context) {
            packageManager.getLaunchIntentForPackage(packageName)
        }

}