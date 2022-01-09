package com.yullg.android.scaffold.helper

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.IntRange
import androidx.annotation.WorkerThread
import java.io.FileDescriptor
import java.io.InputStream
import java.io.OutputStream

/**
 * 提供位图相关的辅助功能
 */
object BitmapHelper {

    /**
     * 压缩图片
     */
    @WorkerThread
    fun compress(
        inputStream: InputStream,
        outputStream: OutputStream,
        maxWidth: Int = 0,
        maxHeight: Int = 0,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        @IntRange(from = 0, to = 100) quality: Int = 100
    ) = doCompress(
        outputStream = outputStream,
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        format = format,
        quality = quality
    ) {
        BitmapFactory.decodeStream(inputStream, null, it)
    }

    /**
     * 压缩图片
     */
    @WorkerThread
    fun compress(
        data: ByteArray,
        offset: Int,
        length: Int,
        outputStream: OutputStream,
        maxWidth: Int = 0,
        maxHeight: Int = 0,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        @IntRange(from = 0, to = 100) quality: Int = 100
    ) = doCompress(
        outputStream = outputStream,
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        format = format,
        quality = quality
    ) {
        BitmapFactory.decodeByteArray(data, offset, length, it)
    }

    /**
     * 压缩图片
     */
    @WorkerThread
    fun compress(
        pathName: String,
        outputStream: OutputStream,
        maxWidth: Int = 0,
        maxHeight: Int = 0,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        @IntRange(from = 0, to = 100) quality: Int = 100
    ) = doCompress(
        outputStream = outputStream,
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        format = format,
        quality = quality
    ) {
        BitmapFactory.decodeFile(pathName, it)
    }

    /**
     * 压缩图片
     */
    @WorkerThread
    fun compress(
        fileDescriptor: FileDescriptor,
        outputStream: OutputStream,
        maxWidth: Int = 0,
        maxHeight: Int = 0,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        @IntRange(from = 0, to = 100) quality: Int = 100
    ) = doCompress(
        outputStream = outputStream,
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        format = format,
        quality = quality
    ) {
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, it)
    }

    /**
     * 压缩图片
     */
    @WorkerThread
    fun compress(
        resources: Resources,
        id: Int,
        outputStream: OutputStream,
        maxWidth: Int = 0,
        maxHeight: Int = 0,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        @IntRange(from = 0, to = 100) quality: Int = 100
    ) = doCompress(
        outputStream = outputStream,
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        format = format,
        quality = quality
    ) {
        BitmapFactory.decodeResource(resources, id, it)
    }

    private inline fun doCompress(
        outputStream: OutputStream,
        maxWidth: Int,
        maxHeight: Int,
        format: Bitmap.CompressFormat,
        @IntRange(from = 0, to = 100) quality: Int,
        block: (BitmapFactory.Options) -> Bitmap?
    ) {
        val options = BitmapFactory.Options()
        if (maxWidth > 0 || maxHeight > 0) {
            // 当指定了最大宽度和最大高度任何一个后开始配置二次采样来缩小图片大小
            options.inJustDecodeBounds = true
            block(options) // 读取图片实际宽高信息
            if (options.outWidth < 0 || options.outHeight < 0) {
                // 如果无法读取图片实际宽高就中断操作
                throw IllegalArgumentException("Invalid decoding result: outWidth = ${options.outWidth}, outHeight = ${options.outHeight}")
            }
            var sampleSize = 1
            while (options.outWidth / sampleSize > maxWidth
                || options.outHeight / sampleSize > maxHeight
            ) {
                sampleSize *= 2
            }
            options.inSampleSize = sampleSize
            options.inJustDecodeBounds = false
        }
        // 解码图片
        val bitmap = block(options)
            ?: throw IllegalArgumentException("Image data could not be decoded")
        // 压缩图片
        bitmap.compress(format, quality, outputStream)
    }

}