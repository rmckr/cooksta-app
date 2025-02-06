package com.example.cooksta_app.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class LogInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (!response.isSuccessful) {
            Log.e("Retrofit", "HTTP Error: $response")
        } else {
            Log.d("Retrofit", "HTTP Response $response")
        }

        return response
    }
}