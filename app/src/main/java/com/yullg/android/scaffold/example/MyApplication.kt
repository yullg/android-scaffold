package com.yullg.android.scaffold.example

import android.app.Application
import android.os.Process
import com.yullg.android.scaffold.support.logger.Logger

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.info("MyApplication.onCreate()--------${Process.myPid()}")
    }

}