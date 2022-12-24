package com.yullg.android.scaffold.support.security

import java.io.InputStream
import java.security.Key
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * 提供散列函数支持
 */
object HashSupport {

    fun md5(): HashOperator = MessageDigestHashOperator(MessageDigest.getInstance("MD5"))

    fun sha1(): HashOperator = MessageDigestHashOperator(MessageDigest.getInstance("SHA-1"))

    fun sha256(): HashOperator = MessageDigestHashOperator(MessageDigest.getInstance("SHA-256"))

    fun sha384(): HashOperator = MessageDigestHashOperator(MessageDigest.getInstance("SHA-384"))

    fun sha512(): HashOperator = MessageDigestHashOperator(MessageDigest.getInstance("SHA-512"))

    fun hmacMD5(key: Key): HashOperator = MacHashOperator(Mac.getInstance("HmacMD5").apply {
        init(key)
    })

    fun hmacSHA1(key: Key): HashOperator = MacHashOperator(Mac.getInstance("HmacSHA1").apply {
        init(key)
    })

    fun hmacSHA256(key: Key): HashOperator = MacHashOperator(Mac.getInstance("HmacSHA256").apply {
        init(key)
    })

    fun hmacSHA384(key: Key): HashOperator = MacHashOperator(Mac.getInstance("HmacSHA384").apply {
        init(key)
    })

    fun hmacSHA512(key: Key): HashOperator = MacHashOperator(Mac.getInstance("HmacSHA512").apply {
        init(key)
    })

    fun hmacMD5(key: ByteArray) = hmacMD5(SecretKeySpec(key, "HmacMD5"))

    fun hmacSHA1(key: ByteArray) = hmacSHA1(SecretKeySpec(key, "HmacSHA1"))

    fun hmacSHA256(key: ByteArray) = hmacSHA256(SecretKeySpec(key, "HmacSHA256"))

    fun hmacSHA384(key: ByteArray) = hmacSHA384(SecretKeySpec(key, "HmacSHA384"))

    fun hmacSHA512(key: ByteArray) = hmacSHA512(SecretKeySpec(key, "HmacSHA512"))

}

private val HEX_CHARS =
    charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

abstract class HashOperator {

    fun operateAsHexString(bytes: ByteArray) = String(encodeHex(operate(bytes)))

    fun operateAsHexString(inputStream: InputStream) = String(encodeHex(operate(inputStream)))

    abstract fun operate(bytes: ByteArray): ByteArray

    abstract fun operate(inputStream: InputStream): ByteArray

    private fun encodeHex(bytes: ByteArray): CharArray {
        val chars = CharArray(32)
        for (i in chars.indices step 2) {
            val b = bytes[i / 2]
            chars[i] = HEX_CHARS[(b.toInt() ushr 0x4) and 0xf]
            chars[i + 1] = HEX_CHARS[b.toInt() and 0xf]
        }
        return chars
    }

}

internal class MessageDigestHashOperator(private val digester: MessageDigest) : HashOperator() {

    override fun operate(bytes: ByteArray): ByteArray {
        return digester.digest(bytes)
    }

    override fun operate(inputStream: InputStream): ByteArray {
        for (b in inputStream.buffered().iterator()) {
            digester.update(b)
        }
        return digester.digest()
    }

}

internal class MacHashOperator(private val mac: Mac) : HashOperator() {

    override fun operate(bytes: ByteArray): ByteArray {
        return mac.doFinal(bytes)
    }

    override fun operate(inputStream: InputStream): ByteArray {
        for (b in inputStream.buffered().iterator()) {
            mac.update(b)
        }
        return mac.doFinal()
    }

}