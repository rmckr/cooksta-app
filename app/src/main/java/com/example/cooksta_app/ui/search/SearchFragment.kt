package com.example.cooksta_app.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.cooksta_app.databinding.FragmentSearchBinding
import com.example.cooksta_app.model.Recipe
import com.example.cooksta_app.network.RetrofitClient
import com.example.cooksta_app.ui.shared.FeedFragment
import com.google.android.material.search.SearchView
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private fun handleSearch(query: String) {
        lifecycleScope.launch {
            val searchResponse = RetrofitClient.apiService.getRecipeFeed() // TODO: Replace with search
            if (searchResponse.isSuccessful) {
                val searchResults = SearchResultsFragment.newInstance(searchResponse.body() ?: emptyList())
                parentFragmentManager.beginTransaction()
                    .replace(binding.searchResultsContainer.id, searchResults)
                    .commit()
            }
        }
    }

    fun handleCategoryClick(category: String) {
        with(binding) {
            searchView.show()
            searchView.editText.setText(category)
            handleSearch(category)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        binding.searchView.addTransitionListener { _, _, newState ->
            if (newState == SearchView.TransitionState.HIDDEN) {
                binding.searchResultsContainer.removeAllViews()
            }
        }

        binding.searchView.editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchView.editText.text.toString()
                handleSearch(query)
            }
            true
        }

        binding.searchAll.setOnClickListener {
            class AllPostsFragment : FeedFragment() {
                override suspend fun providePosts(): List<Recipe> {
                    val feedResponse = RetrofitClient.apiService.getRecipeFeed()
                    if (feedResponse.isSuccessful) {
                        return feedResponse.body() ?: emptyList()
                    }
                    return emptyList()
                }
            }
            val allPostsFragment = AllPostsFragment()
            // TODO: navigate to fragment
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
