package com.example.helpyutilities.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

data class PhotoMetadataSummary(
    val fileSizeBytes: Long,
    val width: Int,
    val height: Int,
    val cameraModel: String,
    val dateTaken: String,
    val hasGpsLocation: Boolean
)

object PhotoPrivacyHelper {
    suspend fun inspectPhoto(context: Context, uri: Uri): PhotoMetadataSummary =
        withContext(Dispatchers.IO) {
            val fileSize = context.contentResolver.openAssetFileDescriptor(uri, "r")?.use {
                it.length
            } ?: 0L

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }

            var camera = "Unknown"
            var date = "None"
            var hasGps = false

            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                val make = exif.getAttribute(ExifInterface.TAG_MAKE) ?: ""
                val model = exif.getAttribute(ExifInterface.TAG_MODEL) ?: ""
                camera = "$make $model".trim().ifEmpty { "None" }
                date = exif.getAttribute(ExifInterface.TAG_DATETIME) ?: "None"
                hasGps = exif.latLong != null
            }

            PhotoMetadataSummary(fileSize, bounds.outWidth, bounds.outHeight, camera, date, hasGps)
        }

    suspend fun compressAndScrubToGallery(
        context: Context,
        sourceUri: Uri,
        quality: Int,
        maxDimensionPx: Int?,
        useWebp: Boolean
    ): Long = withContext(Dispatchers.IO) {
        val originalBitmap = context.contentResolver.openInputStream(sourceUri)?.use {
            BitmapFactory.decodeStream(it)
        } ?: throw IllegalStateException("Could not decode image")

        val scaledBitmap = if (maxDimensionPx != null &&
            (originalBitmap.width > maxDimensionPx || originalBitmap.height > maxDimensionPx)
        ) {
            val ratio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
            val targetW = if (ratio >= 1f) maxDimensionPx else (maxDimensionPx * ratio).toInt()
            val targetH = if (ratio >= 1f) (maxDimensionPx / ratio).toInt() else maxDimensionPx
            Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true)
        } else {
            originalBitmap
        }

        val outStream = ByteArrayOutputStream()
        val format = if (useWebp) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY
            else @Suppress("DEPRECATION") Bitmap.CompressFormat.WEBP
        } else {
            Bitmap.CompressFormat.JPEG
        }

        scaledBitmap.compress(format, quality.coerceIn(10, 100), outStream)
        val cleanBytes = outStream.toByteArray()

        val ext = if (useWebp) "webp" else "jpg"
        val mime = if (useWebp) "image/webp" else "image/jpeg"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Helpy_Clean_${System.currentTimeMillis()}.$ext")
            put(MediaStore.Images.Media.MIME_TYPE, mime)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Helpy")
            }
        }

        val savedUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("Failed to save image")
        context.contentResolver.openOutputStream(savedUri)?.use { it.write(cleanBytes) }
        cleanBytes.size.toLong()
    }
}
