package com.example.cooksta_app.network

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.cooksta_app.LoginActivity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenRefreshAuthenticator(private val context: Context) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Refresh failed, prompt login
        if (response.request.url.encodedPath.toString().contains("/refresh")) {
            runBlocking { handleLogout() }
            return null
        }

        val newAccessToken = runBlocking { RetrofitClient.refreshToken() }
        return if (newAccessToken != null) {
            response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        } else {
            runBlocking { handleLogout() }
            null
        }
    }

    private suspend fun handleLogout() {
        mutex.withLock {
            if (!RetrofitClient.isLoggedIn) { return }

            RetrofitClient.logout()
            Handler(Looper.getMainLooper()).post {
                val intent = Intent(context, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
                Toast.makeText(context, "Die Sitzung ist abgelaufen, bitte erneut anmelden.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
