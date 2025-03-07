package com.example.cooksta_app.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.FragmentSearchBinding
import com.example.cooksta_app.network.RetrofitClient
import com.google.android.material.search.SearchView
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by lazy {
        ViewModelProvider(requireActivity())[SearchViewModel::class.java]
    }

    fun handleCategoryClick(category: CategoryCard) {
        with(binding) {
            searchView.isAutoShowKeyboard = false
            searchView.show()
            searchView.editText.setText(category.textResource)
            viewModel.search(category.target)
            searchView.postDelayed({
                searchView.isAutoShowKeyboard = true
            }, 500)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        binding.searchAll.setOnClickListener {
            lifecycleScope.launch {
                val allPostsResponse = RetrofitClient.apiService.getRecipeFeed(limit = null, page = null)
                val recipeIds = if (allPostsResponse.isSuccessful) allPostsResponse.body()?.map { it.id } ?: emptyList() else emptyList()
                findNavController().navigate(
                    R.id.action_navigation_search_to_feedFragment,
                    Bundle().apply { putStringArray("recipeIds", recipeIds.toTypedArray()) }
                )
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
                viewModel.search(query)
            }
            true
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (binding.searchView.isShowing) {
                binding.searchView.hide()
            } else {
                findNavController().popBackStack()
            }
        }

        lifecycleScope.launch {
            viewModel.results.collect {
                val searchResults = SearchResultsFragment.newInstance(it)
                parentFragmentManager.beginTransaction()
                    .replace(binding.searchResultsContainer.id, searchResults)
                    .commit()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
