package com.example.cooksta_app.ui.home

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cooksta_app.R
import com.example.cooksta_app.network.RetrofitClient
import com.example.cooksta_app.ui.shared.LoadingFragment
import com.example.cooksta_app.ui.shared.feed.FeedFragment
import kotlinx.coroutines.launch

class HomeFragment : LoadingFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            val feedResponse = RetrofitClient.apiService.getRecipeFeed(
                limit = 20,
                page = null
            )
            if (feedResponse.isSuccessful) {
                val recipeIds = feedResponse.body()?.map { it.id } ?: emptyList()
                val currentDestination = findNavController().currentDestination
                if (currentDestination?.getAction(R.id.action_navigation_home_to_navigation_feed) != null && isAdded) {
                    findNavController().navigate(
                        R.id.action_navigation_home_to_navigation_feed,
                        Bundle().apply {
                            putStringArray(FeedFragment.ARG_RECIPE_IDS, recipeIds.toTypedArray())
                            putBoolean(FeedFragment.ARG_IS_HOME_FEED, true)
                        }
                    )
                }
            }
        }
    }
}
