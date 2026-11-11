package com.fluttercandies.flutter_image_compress.exif

import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.File

object Exif {
    // Clockwise rotation that, combined with [isFlipped]'s horizontal mirror,
    // reproduces the canonical EXIF orientation transform. This deliberately
    // differs from ExifInterface.rotationDegrees for TRANSPOSE(5)/TRANSVERSE(7):
    // when paired with a horizontal flip, the rotation that yields the correct
    // result is swapped (5 -> 90, 7 -> 270). Using rotationDegrees here left
    // front-camera selfies rotated 180 + mirrored.
    fun getCorrectionRotation(byteArray: ByteArray): Int {
        return try {
            correctionRotation(ExifInterface(ByteArrayInputStream(byteArray)))
        } catch (e: Exception) {
            0
        }
    }

    fun getCorrectionRotation(file: File): Int {
        return try {
            correctionRotation(ExifInterface(file.absolutePath))
        } catch (e: Exception) {
            0
        }
    }

    private fun correctionRotation(exif: ExifInterface): Int {
        return when (exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90,
            ExifInterface.ORIENTATION_TRANSPOSE -> 90
            ExifInterface.ORIENTATION_ROTATE_180,
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> 180
            ExifInterface.ORIENTATION_ROTATE_270,
            ExifInterface.ORIENTATION_TRANSVERSE -> 270
            else -> 0
        }
    }

    // True for the mirrored EXIF orientations (2/4/5/7). The horizontal mirror
    // these carry (front-camera selfies) is applied via Matrix.postScale.
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
