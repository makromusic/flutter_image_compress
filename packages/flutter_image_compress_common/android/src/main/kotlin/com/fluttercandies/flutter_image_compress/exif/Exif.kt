package com.fluttercandies.flutter_image_compress.exif

import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.File

object Exif {
    fun getRotationDegrees(_bytes: ByteArray): Int {
        return try {
            getFromExifInterface(_bytes)
        } catch (e: Exception) {
            0
        }
    }

    private fun getFromExifInterface(byteArray: ByteArray): Int {
        val exifInterface = ExifInterface(ByteArrayInputStream(byteArray))
        return exifInterface.rotationDegrees
    }

    fun getRotationDegrees(file: File): Int {
        return try {
            ExifInterface(file.absolutePath).rotationDegrees
        } catch (e: Exception) {
            0
        }
    }

    fun isFlipped(byteArray: ByteArray): Boolean {
        return try {
            isFlipped(ExifInterface(ByteArrayInputStream(byteArray)))
        } catch (e: Exception) {
            false
        }
    }

    fun isFlipped(file: File): Boolean {
        return try {
            isFlipped(ExifInterface(file.absolutePath))
        } catch (e: Exception) {
            false
        }
    }

    // True for the mirrored EXIF orientations (2/4/5/7). ExifInterface's
    // rotationDegrees only reports rotation, so the horizontal mirror these
    // carry (front-camera selfies) would otherwise be dropped.
    private fun isFlipped(exif: ExifInterface): Boolean {
        return when (exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
            ExifInterface.ORIENTATION_FLIP_VERTICAL,
            ExifInterface.ORIENTATION_TRANSPOSE,
            ExifInterface.ORIENTATION_TRANSVERSE -> true
            else -> false
        }
    }
}
