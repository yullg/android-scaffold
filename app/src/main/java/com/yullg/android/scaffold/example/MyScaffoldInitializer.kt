package com.yullg.android.scaffold.example

import android.content.Context
import com.yullg.android.scaffold.app.ScaffoldConfig
import com.yullg.android.scaffold.app.ScaffoldInitializer
import com.yullg.android.scaffold.support.logger.AliyunOSSLogUploader
import com.yullg.android.scaffold.support.logger.LogLevel
import java.net.URI
import java.util.concurrent.TimeUnit

class MyScaffoldInitializer : ScaffoldInitializer() {

    override fun configure(context: Context, config: ScaffoldConfig) {
        config.logger {
            fileAppenderLevel = LogLevel.TRACE
            uploadRepeatInterval = Pair(1, TimeUnit.HOURS)
            uploader = AliyunOSSLogUploader(
                URI("https://oss-cn-hangzhou.aliyuncs.com"),
                "",
                "",
                "yullg"
            )
        }
    }

}