package com.example.cooksta_app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.cooksta_app.network.RetrofitClient
import com.example.cooksta_app.ui.shared.FeedFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val frameLayout = FrameLayout(requireContext()).apply {
            id = View.generateViewId()
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        return frameLayout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager.beginTransaction()
            .replace(view.id, FeedFragment())
            .commit()

        lifecycleScope.launch {
            val recipeIds = withContext(Dispatchers.IO) {
                val feedResponse = RetrofitClient.apiService.getRecipeFeed(
                    limit = 20,
                    page = null
                )
                if (feedResponse.isSuccessful) feedResponse.body()?.map { it.id } ?: emptyList() else emptyList()
            }
            val resultBundle = Bundle().apply {
                putStringArrayList("recipeIds", ArrayList(recipeIds))
            }
            childFragmentManager.setFragmentResult("feed", resultBundle)
        }
    }
}
