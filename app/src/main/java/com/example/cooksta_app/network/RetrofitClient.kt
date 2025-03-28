package com.example.cooksta_app.network

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.example.cooksta_app.BuildConfig
import com.example.cooksta_app.model.LoginRequest
import com.example.cooksta_app.model.RefreshTokenRequest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private lateinit var tokenStore: TokenStore

    private lateinit var okHttpClient: OkHttpClient

    private lateinit var retrofit: Retrofit

    private val mutex = Mutex()

    val apiService: APIService by lazy {
        retrofit.create(APIService::class.java)
    }

    fun initialize(context: Context) {
        if (!::tokenStore.isInitialized) {
            tokenStore = TokenStore(context.applicationContext)
        }
        if (!::okHttpClient.isInitialized) {
            okHttpClient = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
                .addInterceptor(NetworkInterceptor(context.applicationContext))
                .addInterceptor(AuthInterceptor(tokenStore))
                .authenticator(TokenRefreshAuthenticator(context.applicationContext))
                .build()
        }
        if (!::retrofit.isInitialized) {
            retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
        }
    }

    val isLoggedIn get() = tokenStore.refreshToken?.isNotEmpty() == true

    @Suppress("unused")
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun useMockServerUrl(url: HttpUrl) {
        this.okHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()
        this.retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    suspend fun login(username: String, password: String): Boolean {
        val response = apiService.login(LoginRequest(username, password))
        if (response.isSuccessful) {
            response.body()?.let {
                tokenStore.saveTokens(it.accessToken, it.refreshToken)
                return@login true
            }
        }
        return false
    }

    suspend fun logout() {
        val refreshToken = tokenStore.refreshToken
        tokenStore.clearTokens()
        refreshToken?.let {
            apiService.logout(RefreshTokenRequest(it))
        }
    }

    suspend fun refreshToken(): String? {
        return mutex.withLock {
            val refreshToken = tokenStore.refreshToken ?: return null
            val response = apiService.refresh(RefreshTokenRequest(refreshToken))
            if (response.isSuccessful) {
                response.body()?.let {
                    tokenStore.saveTokens(it.accessToken, it.refreshToken)
                    it.accessToken
                }
            } else {
                null
            }
        }
    }
}
