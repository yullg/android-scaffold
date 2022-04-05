package com.yullg.android.scaffold.example

import android.content.Context
import com.yullg.android.scaffold.app.ScaffoldConfig
import com.yullg.android.scaffold.app.ScaffoldConstants
import com.yullg.android.scaffold.app.ScaffoldInitializer
import com.yullg.android.scaffold.support.logger.LogLevel

class MyScaffoldInitializer : ScaffoldInitializer() {

    override fun configure(context: Context, config: ScaffoldConfig) {
        config.logger {
            logger(ScaffoldConstants.Logger.NAME_LOGCAT_DUMPER) {
                fileAppenderEnabled = true
            }
//            uploader = AliyunOSSLogUploader(
//                URI("https://oss-cn-hangzhou.aliyuncs.com"),
//                "",
//                "",
//                "yullg"
//            )
        }
    }

}