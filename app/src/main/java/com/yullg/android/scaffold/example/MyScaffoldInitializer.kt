package com.yullg.android.scaffold.example

import android.content.Context
import com.yullg.android.scaffold.app.ScaffoldConfig
import com.yullg.android.scaffold.app.ScaffoldInitializer
import com.yullg.android.scaffold.support.logger.AliyunOSSLogUploader
import com.yullg.android.scaffold.support.logger.LogLevel
import java.net.URI

class MyScaffoldInitializer : ScaffoldInitializer() {

    override fun configure(context: Context, config: ScaffoldConfig) {
        config.logger {
            fileAppenderLevel = LogLevel.TRACE
//            uploader = AliyunOSSLogUploader(
//                URI("https://oss-cn-hangzhou.aliyuncs.com"),
//                "",
//                "",
//                "yullg"
//            )
        }
    }

}