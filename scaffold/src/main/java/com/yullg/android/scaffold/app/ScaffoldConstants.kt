package com.yullg.android.scaffold.app

import com.yullg.android.scaffold.helper.SPHelper
import com.yullg.android.scaffold.internal.CrashLogger
import com.yullg.android.scaffold.internal.ScaffoldLogcat
import com.yullg.android.scaffold.internal.ScaffoldLogger
import com.yullg.android.scaffold.internal.ScaffoldSPHelper
import com.yullg.android.scaffold.support.logger.LogFileDeleteWorker
import com.yullg.android.scaffold.support.logger.LogFileManager
import com.yullg.android.scaffold.support.logger.LogFileUploadWorker
import com.yullg.android.scaffold.support.logger.LogcatDumper
import com.yullg.android.scaffold.support.logger.Logger as SLogger

/**
 * 定义框架编译时常量
 */
object ScaffoldConstants {

    /**
     * 键值存储相关的常量
     */
    object SP {

        /**
         * 框架专用键值存储器的名称常量
         *
         * @see ScaffoldSPHelper
         */
        const val NAME_SCAFFOLD = "yg_sphelper_scaffold"

        /**
         * 默认键值存储器的名称常量
         *
         * @see SPHelper
         */
        const val NAME_DEFAULT = "yg_sphelper_default"

    }

    /**
     * 日志相关的常量
     */
    object Logger {

        /**
         * 日志文件存储目录
         *
         * @see LogFileManager
         */
        const val DIR_LOG = "/yullg/log"

        /**
         * 待上传的日志文件存储目录
         *
         * @see LogFileManager
         */
        const val DIR_LOG_UPLOAD = "/yullg/log/upload"

        /**
         * 标识框架专用日志记录器的日志来源的`TAG`常量
         *
         * @see ScaffoldLogcat
         */
        const val TAG_SCAFFOLD = "yg_logcat_scaffold"

        /**
         * 框架专用日志记录器的名称常量，即[ScaffoldLogger.name]值。
         *
         * @see ScaffoldLogger
         */
        const val NAME_SCAFFOLD = "yg_logger_scaffold"

        /**
         * 默认日志记录器的名称常量，即[SLogger.name]值。
         *
         * @see SLogger
         */
        const val NAME_DEFAULT = "yg_logger_default"

        /**
         * 崩溃日志记录器的名称常量，即[CrashLogger.name]值。
         *
         * @see CrashLogger
         */
        const val NAME_CRASH = "yg_logger_crash"

        /**
         * [LogcatDumper]日志记录器的名称常量
         *
         * @see LogcatDumper
         */
        const val NAME_LOGCAT_DUMPER = "yg_logger_logcat_dumper"

        /**
         * 周期性上传工作的名称常量
         *
         * @see LogFileUploadWorker
         */
        const val WORKER_NAME_UPLOAD_PERIODIC = "YG_PWN_LogUpload"

        /**
         * 一次性上传工作的名称常量
         *
         * @see LogFileUploadWorker
         */
        const val WORKER_NAME_UPLOAD_ONE_TIME = "YG_OWN_LogUpload"

        /**
         * 一次性删除工作的名称常量
         *
         * @see LogFileDeleteWorker
         */
        const val WORKER_NAME_DELETE_ONE_TIME = "YG_OWN_LogDelete"

    }

}