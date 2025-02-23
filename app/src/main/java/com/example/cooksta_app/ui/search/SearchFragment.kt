package com.example.cooksta_app.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.FragmentSearchBinding
import com.example.cooksta_app.network.RetrofitClient
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    fun handleCategoryClick(category: String) {
        with(binding.searchView) {
            show()
            editText.setText(category)
            editText.setOnEditorActionListener { _, _, _ -> true }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        binding.searchAllButton.setOnClickListener {
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

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
