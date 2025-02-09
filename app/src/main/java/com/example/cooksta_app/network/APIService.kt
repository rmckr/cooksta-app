package com.example.cooksta_app.network

import com.example.cooksta_app.model.LoginRequest
import com.example.cooksta_app.model.TokenResponse
import com.example.cooksta_app.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface APIService {
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>
    @POST("/refresh")
    suspend fun refresh(@Body refreshToken: String): Response<TokenResponse>

    @GET("users/me")
    suspend fun getMe(): Response<User>
    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): Response<User>
}
