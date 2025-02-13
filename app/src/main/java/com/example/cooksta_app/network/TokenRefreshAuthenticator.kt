package com.example.cooksta_app.network

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenRefreshAuthenticator(private val tokenStore: TokenStore) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val newAccessToken = runBlocking {
            val refreshToken = tokenStore.refreshToken ?: return@runBlocking null
            Log.d("AUTH", "authenticate: $refreshToken")
            val refreshResponse = RetrofitClient.apiService.refresh(refreshToken)
            if (refreshResponse.isSuccessful) {
                refreshResponse.body()?.let {
                    Log.d("AUTH", "authenticate: $it")
                    tokenStore.saveTokens(it.accessToken, it.refreshToken)
                    it.accessToken
                }
            } else {
                tokenStore.clearTokens()
                null
            }
        }
        return if (newAccessToken != null) {
            response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        } else {
            null
        }
    }
}
