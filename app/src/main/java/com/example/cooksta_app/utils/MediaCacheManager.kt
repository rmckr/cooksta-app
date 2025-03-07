package com.example.cooksta_app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.Log
import com.example.cooksta_app.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MediaCacheManager(private val context: Context) {

    private val cacheDir: File by lazy {
        File(context.cacheDir, "media_cache").apply { mkdirs() }
    }

    private val supportedTypes = mapOf(
        "image/png" to "png",
        "video/mp4" to "mp4"
    )

    suspend fun getMediaFile(id: String): File? {
        supportedTypes.values.forEach { extension ->
            val file = File(cacheDir, "media-$id.$extension")
            if (file.exists()) { return file }
        }

        val mediaResponse = RetrofitClient.apiService.getRecipeMedia(id)
        if (!mediaResponse.isSuccessful) { return null }

        val mediaStream = mediaResponse.body()?.byteStream()
        val contentType = mediaResponse.headers()["Content-Type"]
        val extension = supportedTypes[contentType] ?: return null

        return mediaStream?.let { stream ->
            withContext(Dispatchers.IO) {
                val file = File(cacheDir, "media-$id.$extension").apply { deleteOnExit() }
                file.outputStream().use { output -> stream.copyTo(output) }
                file
            }
        }
    }

    suspend fun getThumbnailFile(id: String): File? {
        val thumbnailFile = File(cacheDir, "$id-thumbnail.jpg")
        if (thumbnailFile.exists()) { return thumbnailFile }

        getThumbnailBitmap(id) ?: return null
        return thumbnailFile
    }

    suspend fun getThumbnailBitmap(id: String): Bitmap? {
        val thumbnailFile = File(cacheDir, "$id-thumbnail.jpg")
        if (thumbnailFile.exists()) {
            return withContext(Dispatchers.IO) {
                BitmapFactory.decodeFile(thumbnailFile.absolutePath)
            }
        }

        val sourceFile = getMediaFile(id) ?: return null
        val mimeType = supportedTypes.filter { it.value.equals(sourceFile.extension, ignoreCase = true) }.keys.firstOrNull() ?: ""

        val bitmap = when {
            mimeType.contains("image") -> {
                BitmapFactory.decodeFile(sourceFile.absolutePath)
            }
            mimeType.contains("video") -> {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(sourceFile.absolutePath)
                val frame = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST)
                retriever.release()
                frame
            }
            else -> null
        } ?: return null

        val thumbnailBitmap = createThumbnailFromBitmap(bitmap)
        return withContext(Dispatchers.IO) {
            FileOutputStream(thumbnailFile).use { out -> thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out) }
            thumbnailBitmap
        }
    }

    private fun createThumbnailFromBitmap(bitmap: Bitmap): Bitmap {
        val size = 128
        with(bitmap) {
            val scale = if (height > width) size.toFloat() / width else size.toFloat() / height
            val scaledWidth = (width * scale).toInt()
            val scaledHeight = (height * scale).toInt()
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)

            val x = (scaledWidth - size) / 2
            val y = (scaledHeight - size) / 2
            return Bitmap.createBitmap(scaledBitmap, x, y, size, size)
        }
    }
}