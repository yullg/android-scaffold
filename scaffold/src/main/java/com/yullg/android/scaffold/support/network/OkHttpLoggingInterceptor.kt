package com.yullg.android.scaffold.support.network

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import okio.GzipSource
import java.io.EOFException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8
import java.util.concurrent.TimeUnit
import kotlin.math.min

/**
 * An OkHttp interceptor which logs request and response information. Can be applied as an
 * [application interceptor][OkHttpClient.interceptors] or as a [OkHttpClient.networkInterceptors].
 *
 * The format of the logs created by this class should not be considered stable and may
 * change slightly between releases. If you need a stable logging format, use your own interceptor.
 */
class OkHttpLoggingInterceptor(
    private val logger: (String) -> Unit,
    private val level: Level = Level.BASIC,
    private val obscureHeaders: Array<String> = emptyArray(),
    private val maxLogBodySize: Long = Long.MAX_VALUE
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (Level.NONE == level) {
            return chain.proceed(request)
        }
        val messageBuilder = StringBuilder()
        try {
            messageBuilder.append("--> ${request.method} ${request.url}")
            chain.connection()?.protocol()?.let {
                messageBuilder.append(" $it")
            }
            val headerMessageEnabled = level.ordinal >= Level.HEADERS.ordinal
            val bodyMessageEnabled = level.ordinal >= Level.BODY.ordinal
            val requestBody = request.body
            if (!headerMessageEnabled && requestBody != null) {
                messageBuilder.append(" (${requestBody.contentLength()}-byte body)")
            }
            if (headerMessageEnabled) {
                val headers = request.headers
                if (requestBody != null) {
                    requestBody.contentType()?.let {
                        if (headers["Content-Type"] == null) {
                            messageBuilder.append("\nContent-Type: $it")
                        }
                    }
                    if (requestBody.contentLength() != -1L) {
                        if (headers["Content-Length"] == null) {
                            messageBuilder.append("\nContent-Length: ${requestBody.contentLength()}")
                        }
                    }
                }
                for (i in 0 until headers.size) {
                    messageBuilder.append("\n${headerMessage(headers, i)}")
                }
                if (!bodyMessageEnabled || requestBody == null) {
                    messageBuilder.append("\n--> END ${request.method}")
                } else if (bodyHasUnknownEncoding(request.headers)) {
                    messageBuilder.append("\n--> END ${request.method} (encoded body omitted)")
                } else if (requestBody.isDuplex()) {
                    messageBuilder.append("\n--> END ${request.method} (duplex request body omitted)")
                } else if (requestBody.isOneShot()) {
                    messageBuilder.append("\n--> END ${request.method} (one-shot body omitted)")
                } else {
                    var buffer = Buffer()
                    requestBody.writeTo(buffer)
                    var gzippedLength: Long? = null
                    if ("gzip".equals(headers["Content-Encoding"], ignoreCase = true)) {
                        gzippedLength = buffer.size
                        GzipSource(buffer.clone()).use { gzippedResponseBody ->
                            buffer = Buffer()
                            buffer.writeAll(gzippedResponseBody)
                        }
                    }
                    val charset: Charset = requestBody.contentType()?.charset(UTF_8) ?: UTF_8
                    if (buffer.isProbablyUtf8()) {
                        messageBuilder.append(
                            "\n\n${
                                buffer.clone().readString(min(buffer.size, maxLogBodySize), charset)
                            }"
                        )
                        if (gzippedLength != null) {
                            messageBuilder.append("\n--> END ${request.method} (${buffer.size}-byte body, $gzippedLength-gzipped-byte body)")
                        } else {
                            messageBuilder.append("\n--> END ${request.method} (${buffer.size}-byte body)")
                        }
                    } else {
                        messageBuilder.append(
                            "\n--> END ${request.method} (binary ${buffer.size}-byte body omitted)"
                        )
                    }
                }
            }

            val response: Response
            val startNs = System.nanoTime()
            try {
                response = chain.proceed(request)
            } catch (e: Exception) {
                messageBuilder.append("\n\n<-- HTTP FAILED: $e")
                throw e
            }

            val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
            val responseBody = response.body
            val bodySize = responseBody?.contentLength()?.let {
                if (it != -1L) "$it-byte" else null
            } ?: "unknown-length"
            messageBuilder.append(
                "\n\n<-- ${response.code}${if (response.message.isEmpty()) "" else ' ' + response.message} ${response.request.url} (${tookMs}ms${if (headerMessageEnabled) "" else ", $bodySize body"})"
            )
            if (headerMessageEnabled) {
                val headers = response.headers
                for (i in 0 until headers.size) {
                    messageBuilder.append("\n${headerMessage(headers, i)}")
                }
                if (!bodyMessageEnabled || responseBody == null) {
                    messageBuilder.append("\n<-- END HTTP")
                } else if (bodyHasUnknownEncoding(response.headers)) {
                    messageBuilder.append("\n<-- END HTTP (encoded body omitted)")
                } else {
                    val source = responseBody.source()
                    source.request(Long.MAX_VALUE)
                    var buffer = source.buffer
                    var gzippedLength: Long? = null
                    if ("gzip".equals(headers["Content-Encoding"], ignoreCase = true)) {
                        gzippedLength = buffer.size
                        GzipSource(buffer.clone()).use { gzippedResponseBody ->
                            buffer = Buffer()
                            buffer.writeAll(gzippedResponseBody)
                        }
                    }
                    val charset: Charset = responseBody.contentType()?.charset(UTF_8) ?: UTF_8
                    if (buffer.isProbablyUtf8()) {
                        messageBuilder.append(
                            "\n\n${
                                buffer.clone().readString(min(buffer.size, maxLogBodySize), charset)
                            }"
                        )
                        if (gzippedLength != null) {
                            messageBuilder.append("\n<-- END HTTP (${buffer.size}-byte body, $gzippedLength-gzipped-byte body)")
                        } else {
                            messageBuilder.append("\n<-- END HTTP (${buffer.size}-byte body)")
                        }
                    } else {
                        messageBuilder.append(
                            "\n<-- END HTTP (binary ${buffer.size}-byte body omitted)"
                        )
                    }
                }
            }
            return response
        } finally {
            logger(messageBuilder.toString())
        }
    }

    private fun headerMessage(headers: Headers, i: Int): String {
        val name = headers.name(i)
        val value = if (obscureHeaders.any { it.equals(name, true) }) "██" else headers.value(i)
        return "$name: $value"
    }

    private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"] ?: return false
        return !contentEncoding.equals("identity", ignoreCase = true) &&
                !contentEncoding.equals("gzip", ignoreCase = true)
    }

    enum class Level {
        /** No logs. */
        NONE,

        /**
         * Logs request and response lines.
         *
         * Example:
         * ```
         * --> POST /greeting http/1.1 (3-byte body)
         *
         * <-- 200 OK (22ms, 6-byte body)
         * ```
         */
        BASIC,

        /**
         * Logs request and response lines and their respective headers.
         *
         * Example:
         * ```
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
         * ```
         */
        HEADERS,

        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         *
         * Example:
         * ```
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
         * ```
         */
        BODY
    }

}

/**
 * Returns true if the body in question probably contains human readable text. Uses a small
 * sample of code points to detect unicode control characters commonly used in binary file
 * signatures.
 */
private fun Buffer.isProbablyUtf8(): Boolean {
    try {
        val prefix = Buffer()
        val byteCount = size.coerceAtMost(64)
        copyTo(prefix, 0, byteCount)
        for (i in 0 until 16) {
            if (prefix.exhausted()) {
                break
            }
            val codePoint = prefix.readUtf8CodePoint()
            if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                return false
            }
        }
        return true
    } catch (_: EOFException) {
        return false // Truncated UTF-8 sequence.
    }
}