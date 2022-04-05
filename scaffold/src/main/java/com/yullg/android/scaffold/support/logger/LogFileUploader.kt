package com.yullg.android.scaffold.support.logger

import com.yullg.android.scaffold.helper.SettingsHelper
import com.yullg.android.scaffold.internal.AliyunOSSClient
import java.net.URI

/**
 * 定义日志文件上传接口
 */
interface LogFileUploader {

    /**
     * 执行文件上传操作。
     * 该方法正常结束表示上传成功；该方法异常结束表示上传失败。
     */
    fun upload(logFile: LogFile)

}

/**
 * 将日志文件上传到阿里云OSS的[LogFileUploader]实现
 */
class AliyunOSSLogFileUploader(
    endpointURI: URI,
    accessKeyId: String,
    accessKeySecret: String,
    private val bucketName: String,
    private val objectKeyGetter: (LogFile) -> String = { "${SettingsHelper.SSAID}/${it.file.name}" }
) : LogFileUploader {

    private val ossClient = AliyunOSSClient(endpointURI, accessKeyId, accessKeySecret)

    override fun upload(logFile: LogFile) {
        logFile.file.inputStream().use {
            ossClient.appendObject(bucketName, objectKeyGetter(logFile), it)
        }
    }

}