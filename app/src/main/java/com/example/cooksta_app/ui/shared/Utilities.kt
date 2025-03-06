package com.example.cooksta_app.ui.shared

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Log
import com.bumptech.glide.Glide
import com.example.cooksta_app.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

object Utilities {
    suspend fun fetchVideoThumbnail(context: Context, recipeId: String): Bitmap? {
        return try {
            val response = RetrofitClient.apiService.getRecipeMedia(recipeId)
            if (response.isSuccessful) {
                val mediaType = response.headers()["Content-Type"]
                val mediaStream = response.body()?.byteStream()

                if (mediaType?.contains("image") == true) {
                    val bitmap = withContext(Dispatchers.IO) {
                        Glide.with(context)
                            .asBitmap()
                            .load(mediaStream?.readBytes())
                            .submit()
                            .get()
                    }
                    return bitmap
                } else if (mediaType?.contains("video") == true) {
                    val bitmap = extractThumbnail(mediaStream)
                    if (bitmap !== null) {
                        return bitmap
                    }
                }
                if (mediaType?.contains("video") == true) {
                    return extractThumbnail(mediaStream)
                }
            }
            null
        } catch (e: Exception) {
            Log.e("Utilities", "Fehler beim Laden des Media oder Extrahieren des Thumbnails", e)
            null
        }
    }

    private fun extractThumbnail(inputStream: InputStream?): Bitmap? {
        if (inputStream == null) return null
        val retriever = MediaMetadataRetriever()
        return try {
            val tempFile = File.createTempFile("video_", ".mp4")
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            retriever.setDataSource(tempFile.absolutePath)
            retriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST)
        } catch (e: Exception) {
            Log.e("getVideoThumbnail", "Fehler beim Extrahieren des Frames", e)
            null
        } finally {
            retriever.release()
        }
    }
}