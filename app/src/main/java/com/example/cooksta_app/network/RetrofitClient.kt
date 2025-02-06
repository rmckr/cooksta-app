package com.example.cooksta_app.network

import android.content.Context
import com.example.cooksta_app.BuildConfig
import com.example.cooksta_app.model.LoginRequest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val mutex = Mutex()

    private lateinit var tokenStore: TokenStore

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(LogInterceptor())
            .addInterceptor(AuthInterceptor(tokenStore))
            .authenticator(TokenRefreshAuthenticator(tokenStore))
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL + "/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    val apiService by lazy {
        retrofit.create(APIService::class.java)
    }

    fun initialize(context: Context) {
        if (!::tokenStore.isInitialized) {
            this.tokenStore = TokenStore(context.applicationContext)
        }
    }

    val isLoggedIn get() = this.tokenStore.refreshToken?.isNotEmpty() ?: false

    suspend fun login(username: String, password: String): Boolean {
        val response = apiService.login(LoginRequest(username, password))
        if (response.isSuccessful) {
            response.body()?.let {
                this.tokenStore.saveTokens(it.accessToken, it.refreshToken)
                return@login true
            }
        }
        return false
    }

    suspend fun refreshToken(): String? {
        return mutex.withLock {
            val refreshToken = this.tokenStore.refreshToken ?: return null
            val response = apiService.refresh(refreshToken)
            if (response.isSuccessful) {
                response.body()?.let {
                    this.tokenStore.saveTokens(it.accessToken, it.refreshToken)
                    it.accessToken
                }
            } else {
                this.tokenStore.clearTokens() // Clear tokens if refresh fails
                null
            }
        }
    }
}
