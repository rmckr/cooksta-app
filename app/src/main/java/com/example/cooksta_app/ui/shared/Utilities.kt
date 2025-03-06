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
    // TODO: Cache thumbnails
    suspend fun fetchVideoThumbnail(context: Context, recipeId: String): Bitmap? {
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
                return extractThumbnail(mediaStream)
            }
        }
        return null
    }

    private fun extractThumbnail(inputStream: InputStream?): Bitmap? {
        if (inputStream == null) return null
        val retriever = MediaMetadataRetriever()

        val tempFile = File.createTempFile("video_", ".mp4")
        tempFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        retriever.setDataSource(tempFile.absolutePath)
        val bitmap = retriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST)
        retriever.release()

        return bitmap
    }
}