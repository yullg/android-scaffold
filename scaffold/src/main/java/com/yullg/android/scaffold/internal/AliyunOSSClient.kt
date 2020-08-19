package com.yullg.android.scaffold.internal

import android.util.Base64
import android.webkit.MimeTypeMap
import androidx.annotation.RestrictTo
import com.yullg.android.scaffold.helper.DigestHelper
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

@RestrictTo(RestrictTo.Scope.LIBRARY)
class AliyunOSSClient(
    private val endpointURI: URI,
    private val accessKeyId: String,
    private val accessKeySecret: String
) {

    fun headObject(bucketName: String, objectKey: String): Map<String, List<String>> {
        return object : RequestTask(REQUEST_METHOD_HEAD, bucketName, objectKey) {
        }.run {
            headerFields
        }
    }

    fun putObject(bucketName: String, objectKey: String, inputStream: InputStream) {
        object : RequestTask(REQUEST_METHOD_PUT, bucketName, objectKey) {
            override fun sendData(httpURLConnection: HttpURLConnection) {
                httpURLConnection.outputStream.use {
                    inputStream.copyTo(it)
                }
            }
        }.run {}
    }

    fun appendObject(bucketName: String, objectKey: String, inputStream: InputStream) {
        val position = object : RequestTask(REQUEST_METHOD_HEAD, bucketName, objectKey) {
            override fun verifyResponse(httpURLConnection: HttpURLConnection) {
                if (httpURLConnection.responseCode == 404) return
                super.verifyResponse(httpURLConnection)
            }
        }.run {
            getHeaderField("x-oss-next-append-position")?.let {
                try {
                    it.toLong()
                } catch (e: Exception) {
                    ScaffoldLogger.warn(
                        "[AliyunOSSClient] Header field parse failed : x-oss-next-append-position = $it",
                        e
                    )
                    null
                }
            } ?: 0L
        }
        object :
            RequestTask(REQUEST_METHOD_POST, bucketName, "$objectKey?append&position=$position") {
            override fun sendData(httpURLConnection: HttpURLConnection) {
                httpURLConnection.outputStream.use {
                    inputStream.copyTo(it)
                }
            }
        }.run {}
    }

    private abstract inner class RequestTask(
        val requestMethod: String,
        val bucketName: String,
        val resource: String
    ) {

        val url = URL(endpointURI.scheme, endpointURI.host, resource)

        open fun <T> run(block: HttpURLConnection.() -> T): T {
            val logContent = StringBuilder()
            val logEnabled = ScaffoldLogger.isDebugEnabled()
            val httpURLConnection = url.openConnection() as HttpURLConnection
            try {
                prepareConnection(httpURLConnection)
                if (logEnabled) {
                    httpURLConnection.requestProperties?.let {
                        for (entry in it) {
                            logContent.append("Request Header [${entry.key}]: ${entry.value}\n")
                        }
                    }
                }
                httpURLConnection.connect()
                sendData(httpURLConnection)
                if (logEnabled) {
                    httpURLConnection.headerFields?.let {
                        for (entry in it) {
                            logContent.append("Response Header [${entry.key}]: ${entry.value}\n")
                        }
                    }
                    try {
                        httpURLConnection.errorStream?.use {
                            logContent.append("Response Error Body: ${it.reader().readText()}")
                        }
                    } catch (e: Exception) {
                        logContent.append("Response Error Body: parse failed ($e)")
                    }
                }
                verifyResponse(httpURLConnection)
                return httpURLConnection.block()
            } finally {
                httpURLConnection.disconnect()
                if (logEnabled) {
                    ScaffoldLogger.debug("[AliyunOSSClient] Http connection -> $requestMethod : $url\n$logContent")
                }
            }
        }

        open fun prepareConnection(httpURLConnection: HttpURLConnection) {
            httpURLConnection.requestMethod = requestMethod
            val contentType = determineContentType(resource)
            val dateStr = formatRfc822Date(Date())
            httpURLConnection.setRequestProperty(
                REQUEST_HEADER_AUTHORIZATION,
                computeSignature(
                    accessKeyId,
                    accessKeySecret,
                    requestMethod,
                    contentType,
                    dateStr,
                    null,
                    "/$bucketName/$resource"
                )
            )
            httpURLConnection.setRequestProperty(REQUEST_HEADER_CONTENT_TYPE, contentType)
            httpURLConnection.setRequestProperty(REQUEST_HEADER_DATE, dateStr)
            httpURLConnection.setRequestProperty(
                REQUEST_HEADER_HOST,
                "$bucketName.${endpointURI.host}"
            )
        }

        open fun sendData(httpURLConnection: HttpURLConnection) {}

        open fun verifyResponse(httpURLConnection: HttpURLConnection) {
            if (httpURLConnection.responseCode == 203 || httpURLConnection.responseCode >= 300) {
                throw AliyunOSSException("Server response error [$requestMethod : $url : ${httpURLConnection.responseCode}]")
            }
        }

    }

}

@RestrictTo(RestrictTo.Scope.LIBRARY)
class AliyunOSSException(message: String? = null) : RuntimeException(message)

private const val REQUEST_METHOD_HEAD = "HEAD"
private const val REQUEST_METHOD_PUT = "PUT"
private const val REQUEST_METHOD_POST = "POST"
private const val REQUEST_HEADER_AUTHORIZATION = "Authorization"
private const val REQUEST_HEADER_CONTENT_TYPE = "Content-Type"
private const val REQUEST_HEADER_DATE = "Date"
private const val REQUEST_HEADER_HOST = "Host"
private const val RFC822_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'"

private fun computeSignature(
    accessKeyId: String,
    accessKeySecret: String,
    requestMethod: String,
    contentType: String,
    dateStr: String,
    headers: String?,
    resource: String
): String {
    val binaryData = DigestHelper.hmacSHA1(accessKeySecret.toByteArray(Charsets.UTF_8))
        .digest(
            "$requestMethod\n\n$contentType\n$dateStr\n${
                if (headers.isNullOrBlank()) "" else "$headers\n"
            }$resource".toByteArray(Charsets.UTF_8)
        )
    return "OSS " + accessKeyId + ":" + String(Base64.encode(binaryData, Base64.DEFAULT)).trim()
}

private fun formatRfc822Date(date: Date): String {
    val dateFormat = SimpleDateFormat(RFC822_DATE_FORMAT, Locale.US)
    dateFormat.timeZone = SimpleTimeZone(0, "GMT")
    return dateFormat.format(date)
}

private fun determineContentType(resource: String): String {
    val typeMap = MimeTypeMap.getSingleton()
    val extension = getFileExtensionFromUrl(resource)
    val contentType = typeMap.getMimeTypeFromExtension(extension)
    return contentType ?: "application/octet-stream"
}

private fun getFileExtensionFromUrl(url: String): String {
    var url = url
    if (url.isNotBlank()) {
        val fragment = url.lastIndexOf('#')
        if (fragment > 0) {
            url = url.substring(0, fragment)
        }
        val query = url.lastIndexOf('?')
        if (query > 0) {
            url = url.substring(0, query)
        }
        val dotPos = url.lastIndexOf('.')
        if (dotPos >= 0) {
            return url.substring(dotPos + 1)
        }
    }
    return ""
}