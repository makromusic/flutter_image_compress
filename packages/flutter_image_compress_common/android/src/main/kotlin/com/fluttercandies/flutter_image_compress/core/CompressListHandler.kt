package com.fluttercandies.flutter_image_compress.core

import android.content.Context
import com.fluttercandies.flutter_image_compress.ImageCompressPlugin
import com.fluttercandies.flutter_image_compress.exception.CompressError
import com.fluttercandies.flutter_image_compress.exif.Exif
import com.fluttercandies.flutter_image_compress.format.FormatRegister
import com.fluttercandies.flutter_image_compress.logger.log
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.io.ByteArrayOutputStream

class CompressListHandler(private val call: MethodCall, result: MethodChannel.Result) : ResultHandler(result) {
    fun handle(context: Context) {
        threadPool.execute {
            @Suppress("UNCHECKED_CAST") val args: List<Any> = call.arguments as List<Any>
            val arr = args[0] as ByteArray
            var minWidth = args[1] as Int
            var minHeight = args[2] as Int
            val quality = args[3] as Int
            val rawRotate = args[4] as Int
            val autoCorrectionAngle = args[5] as Boolean
            val format = args[6] as Int
            val keepExif = args[7] as Boolean
            val inSampleSize = args[8] as Int
            // A NEGATIVE rotate is a sentinel from Dart requesting an explicit
            // horizontal mirror (the public API exposes `rotate` but no flip).
            // It carries no rotation; the mirror is applied via Matrix.postScale
            // in rotate(). This lets callers flip an already-upright image
            // losslessly without round-tripping through EXIF orientation tags.
            val flipRequested = rawRotate < 0
            val rotate = if (flipRequested) 0 else rawRotate
            val exifRotate = if (autoCorrectionAngle) Exif.getCorrectionRotation(arr) else 0
            val exifFlip = if (autoCorrectionAngle) Exif.isFlipped(arr) else false
            val flip = exifFlip xor flipRequested
            if (exifRotate == 270 || exifRotate == 90) {
                val tmp = minWidth
                minWidth = minHeight
                minHeight = tmp
            }
            val formatHandler = FormatRegister.findFormat(format)
            if (formatHandler == null) {
                log("No support format.")
                reply(null)
                return@execute
            }
            val targetRotate = rotate + exifRotate
            val outputStream = ByteArrayOutputStream()
            try {
                formatHandler.handleByteArray(
                    context,
                    arr,
                    outputStream,
                    minWidth,
                    minHeight,
                    quality,
                    targetRotate,
                    flip,
                    keepExif,
                    inSampleSize
                )
                reply(outputStream.toByteArray())
            } catch (e: CompressError) {
                log(e.message)
                if (ImageCompressPlugin.showLog) e.printStackTrace()
                reply(null)
            } catch (e: Exception) {
                if (ImageCompressPlugin.showLog) e.printStackTrace()
                reply(null)
            } finally {
                outputStream.close()
            }
        }
    }
}
