package com.example.cooksta_app.ui.home

import com.example.cooksta_app.network.RetrofitClient
import com.example.cooksta_app.ui.shared.FeedFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeFragment : FeedFragment() {
    override suspend fun provideRecipeIds(): List<String> {
        return withContext(Dispatchers.IO) {
            val feedResponse = RetrofitClient.apiService.getRecipeFeed(
                limit = 20,
                page = null
            )
            return@withContext if (feedResponse.isSuccessful) feedResponse.body()?.map { it.id } ?: emptyList() else emptyList()
        }
    }
}
