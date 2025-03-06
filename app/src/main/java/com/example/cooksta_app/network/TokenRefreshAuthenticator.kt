package com.example.cooksta_app.network

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenRefreshAuthenticator : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val newAccessToken = runBlocking { RetrofitClient.refreshToken() }
        return if (newAccessToken != null) {
            response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        } else {
            null
        }
    }
}
