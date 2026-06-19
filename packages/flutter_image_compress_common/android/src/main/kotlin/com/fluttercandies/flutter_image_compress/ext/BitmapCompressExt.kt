package com.fluttercandies.flutter_image_compress.ext

import android.graphics.Bitmap
import android.graphics.Matrix
import com.fluttercandies.flutter_image_compress.ImageCompressPlugin
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import kotlin.math.max
import kotlin.math.min

fun Bitmap.compress(minWidth: Int, minHeight: Int, quality: Int, rotate: Int = 0, flip: Boolean = false, format: Int): ByteArray {
    val outputStream = ByteArrayOutputStream()
    compress(minWidth, minHeight, quality, rotate, flip, outputStream, format)
    return outputStream.toByteArray()
}

fun Bitmap.compress(
    minWidth: Int,
    minHeight: Int,
    quality: Int,
    rotate: Int = 0,
    flip: Boolean = false,
    outputStream: OutputStream,
    format: Int = 0
) {
    val w = this.width.toFloat()
    val h = this.height.toFloat()
    log("src width = $w")
    log("src height = $h")
    val scale = calcScale(minWidth, minHeight)
    log("scale = $scale")
    val destW = w / scale
    val destH = h / scale
    log("dst width = $destW")
    log("dst height = $destH")
    Bitmap.createScaledBitmap(
        this,
        destW.toInt(),
        destH.toInt(), true
    ).rotate(rotate, flip).compress(convertFormatIndexToFormat(format), quality, outputStream)
}

private fun log(any: Any?) {
    if (ImageCompressPlugin.showLog) {
        println(any ?: "null")
    }
}

fun Bitmap.rotate(rotate: Int, flip: Boolean = false): Bitmap {
    if (rotate % 360 == 0 && !flip) return this
    val matrix = Matrix()
    matrix.setRotate(rotate.toFloat())
    // Apply the horizontal mirror carried by flipped EXIF orientations (2/4/5/7);
    // ExifInterface.rotationDegrees drops it, leaving front-camera selfies wrong.
    if (flip) matrix.postScale(-1f, 1f)
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, false)
}

fun Bitmap.calcScale(minWidth: Int, minHeight: Int): Float {
    val w = width.toFloat()
    val h = height.toFloat()
    val scaleW = w / minWidth.toFloat()
    val scaleH = h / minHeight.toFloat()
    log("width scale = $scaleW")
    log("height scale = $scaleH")
    return max(1f, min(scaleW, scaleH))
}

fun convertFormatIndexToFormat(type: Int): Bitmap.CompressFormat {
    return when (type) {
        1 -> Bitmap.CompressFormat.PNG
        3 -> Bitmap.CompressFormat.WEBP
        else -> Bitmap.CompressFormat.JPEG
    }
}
