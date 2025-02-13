package com.example.cooksta_app.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)

data class StateResponse(
    val state: Boolean
)