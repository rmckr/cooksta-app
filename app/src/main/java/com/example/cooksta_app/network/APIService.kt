package com.example.cooksta_app.network

import com.example.cooksta_app.model.User
import retrofit2.http.GET
import retrofit2.http.Path

interface APIService {
    @GET("users/{userId}")
    suspend fun getUserInfo(@Path("userId") userId: String): User
}
