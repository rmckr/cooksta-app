package com.example.cooksta_app.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.FragmentSearchBinding
import com.example.cooksta_app.network.RetrofitClient
import com.google.android.material.search.SearchView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private fun handleSearch(query: String) {
        lifecycleScope.launch {
            val searchResponse = RetrofitClient.apiService.searchRecipes(query)
            if (searchResponse.isSuccessful) {
                val searchResults = SearchResultsFragment.newInstance(searchResponse.body() ?: emptyList())
                parentFragmentManager.beginTransaction()
                    .replace(binding.searchResultsContainer.id, searchResults)
                    .commit()
            }
        }
    }

    fun handleCategoryClick(category: CategoryCard) {
        with(binding) {
            searchView.show()
            searchView.editText.setText(category.textResource)
            handleSearch(category.target)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        binding.searchAll.setOnClickListener {
            findNavController().navigate(R.id.feedFragment)
            lifecycleScope.launch {
                val allPostsResponse = RetrofitClient.apiService.getRecipeFeed(limit = null, page = null)
                val recipeIds = if (allPostsResponse.isSuccessful) allPostsResponse.body()?.map { it.id } ?: emptyList() else emptyList()
                val resultBundle = Bundle().apply {
                    putStringArrayList("recipeIds", ArrayList(recipeIds))
                }
                parentFragmentManager.setFragmentResult("feed", resultBundle)
            }
        }

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

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
