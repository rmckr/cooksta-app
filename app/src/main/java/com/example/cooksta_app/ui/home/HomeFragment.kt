package com.example.cooksta_app.ui.home

import com.example.cooksta_app.model.Recipe
import com.example.cooksta_app.network.RetrofitClient
import com.example.cooksta_app.ui.shared.FeedFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeFragment : FeedFragment() {
    override suspend fun providePosts(): List<Recipe> {
        return withContext(Dispatchers.IO) {
            val feedResponse = RetrofitClient.apiService.getRecipeFeed()
            return@withContext if (feedResponse.isSuccessful) feedResponse.body() ?: emptyList() else emptyList()
        }
    }
}
