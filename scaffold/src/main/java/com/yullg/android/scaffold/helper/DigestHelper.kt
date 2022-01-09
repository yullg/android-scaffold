package com.yullg.android.scaffold.helper

import java.io.InputStream
import java.security.Key
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * 提供消息摘要相关的辅助功能
 */
object DigestHelper {

    private val ALGORITHM_MD5 = "MD5"
    private val ALGORITHM_SHA1 = "SHA-1"
    private val ALGORITHM_SHA256 = "SHA-256"
    private val ALGORITHM_HMAC_MD5 = "HmacMD5"
    private val ALGORITHM_HMAC_SHA1 = "HmacSHA1"
    private val ALGORITHM_HMAC_SHA256 = "HmacSHA256"

    fun md5() = MessageDigestProxy(MessageDigest.getInstance(ALGORITHM_MD5))

    fun sha1() = MessageDigestProxy(MessageDigest.getInstance(ALGORITHM_SHA1))

    fun sha256() = MessageDigestProxy(MessageDigest.getInstance(ALGORITHM_SHA256))

    fun hmacMD5(key: Key) = MacProxy(Mac.getInstance(ALGORITHM_HMAC_MD5).apply {
        init(key)
    })

    fun hmacSHA1(key: Key) = MacProxy(Mac.getInstance(ALGORITHM_HMAC_SHA1).apply {
        init(key)
    })

    fun hmacSHA256(key: Key) = MacProxy(Mac.getInstance(ALGORITHM_HMAC_SHA256).apply {
        init(key)
    })

    fun hmacMD5(key: ByteArray) = hmacMD5(SecretKeySpec(key, ALGORITHM_HMAC_MD5))

    fun hmacSHA1(key: ByteArray) = hmacSHA1(SecretKeySpec(key, ALGORITHM_HMAC_SHA1))

    fun hmacSHA256(key: ByteArray) = hmacSHA256(SecretKeySpec(key, ALGORITHM_HMAC_SHA256))

    class MessageDigestProxy internal constructor(private val messageDigest: MessageDigest) {

        fun digestAsHexString(bytes: ByteArray) = String(encodeHex(digest(bytes)))

        fun digestAsHexString(inputStream: InputStream) = String(encodeHex(digest(inputStream)))

        fun digest(bytes: ByteArray): ByteArray = messageDigest.digest(bytes)

        fun digest(inputStream: InputStream): ByteArray {
            for (b in inputStream.buffered().iterator()) {
                messageDigest.update(b)
            }
            return messageDigest.digest()
        }

    }

    class MacProxy internal constructor(private val mac: Mac) {

        fun digestAsHexString(bytes: ByteArray) = String(encodeHex(digest(bytes)))

        fun digestAsHexString(inputStream: InputStream) = String(encodeHex(digest(inputStream)))

        fun digest(bytes: ByteArray): ByteArray = mac.doFinal(bytes)

        fun digest(inputStream: InputStream): ByteArray {
            for (b in inputStream.buffered().iterator()) {
                mac.update(b)
            }
            return mac.doFinal()
        }

    }

}

private val HEX_CHARS =
    charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

private fun encodeHex(bytes: ByteArray): CharArray {
    val chars = CharArray(32)
    for (i in chars.indices step 2) {
        val b = bytes[i / 2]
        chars[i] = HEX_CHARS[(b.toInt() ushr 0x4) and 0xf]
        chars[i + 1] = HEX_CHARS[b.toInt() and 0xf]
    }
    return chars
}