package com.example.cooksta_app.network

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.IOException

class NetworkInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            chain.proceed(chain.request())
        } catch (e: IOException) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "No API connection", Toast.LENGTH_SHORT).show()
            }

            // Handle network errors and return a fake unsuccessful response
            val errorJson = """{ "message": "No API connection" }"""
            Response.Builder()
                .code(503)
                .message("No internet connection")
                .protocol(Protocol.HTTP_1_1)
                .request(chain.request())
                .body(errorJson.toResponseBody("application/json".toMediaTypeOrNull()))
                .build()
        }
    }
}