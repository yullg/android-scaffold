package com.yullg.android.scaffold.example

import android.app.Application
import android.os.Process
import com.yullg.android.scaffold.core.ApplicationCrashHandler
import com.yullg.android.scaffold.support.logger.LogcatDumper
import com.yullg.android.scaffold.support.logger.Logger

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LogcatDumper.mount()
        ApplicationCrashHandler.install()
        Logger.info("MyApplication.onCreate()--------${Process.myPid()}")
    }

}