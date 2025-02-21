package com.example.cooksta_app.ui.shared

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Log
import java.io.File
import java.io.InputStream

object Utilities {
    fun getVideoThumbnail(inputStream: InputStream?): Bitmap? {
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