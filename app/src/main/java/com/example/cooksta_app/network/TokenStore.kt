package com.example.cooksta_app.network

import android.content.Context

class TokenStore(private val context: Context) {

    private var sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    val accessToken get() = sharedPreferences.getString("accessToken", null)

    val refreshToken get() = sharedPreferences.getString("refreshToken", null)

    fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPreferences.edit()
            .putString("accessToken", accessToken)
            .putString("refreshToken", refreshToken)
            .apply()
    }

    fun clearTokens() {
        sharedPreferences.edit().clear().apply()
    }
}
