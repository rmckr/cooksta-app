package com.example.cooksta_app.ui.search

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.ItemSearchResultBinding
import com.example.cooksta_app.model.Recipe
import com.example.cooksta_app.ui.shared.FeedFragment
import com.example.cooksta_app.ui.shared.RecyclerViewFragment
import com.example.cooksta_app.ui.shared.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchResultsFragment : RecyclerViewFragment<SearchResultsFragment.SearchResultsAdapter>() {

    private val recipes: ArrayList<Recipe> by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelableArrayList(ARG_RECIPES, Recipe::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelableArrayList(ARG_RECIPES)
        } ?: throw IllegalStateException("recipes argument is missing")
    }

    companion object {
        private const val ARG_RECIPES = "recipes"

        fun newInstance(recipes: List<Recipe>): SearchResultsFragment {
            val args = Bundle()
            args.putParcelableArrayList(ARG_RECIPES, ArrayList(recipes))
            val fragment = SearchResultsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    inner class SearchResultsAdapter : RecyclerView.Adapter<SearchResultsAdapter.SearchResultViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
            val binding = ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SearchResultViewHolder(binding)
        }

        override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
            val recipe = recipes[position]
            holder.bind(recipe)
        }

        override fun getItemCount(): Int {
            return recipes.size
        }

        inner class SearchResultViewHolder(private val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(recipe: Recipe) {
                binding.searchRecipeName.text = recipe.title
                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = Utilities.fetchVideoThumbnail(binding.searchRecipePicture.context, recipe.id)
                    if (bitmap != null) {
                        withContext(Dispatchers.Main) {
                            binding.searchRecipePicture.load(bitmap)
                        }
                    }
                }

                binding.searchResultCard.setOnClickListener {
                    findNavController(binding.root).navigate(
                        R.id.action_navigation_search_to_feedFragment,
                        Bundle().apply {
                            putStringArray(FeedFragment.ARG_RECIPE_IDS, recipes.map { it.id }.toTypedArray())
                            putInt(FeedFragment.ARG_POSITION, adapterPosition)
                        }
                    )
                }
            }
        }
    }

    override fun provideAdapter(): SearchResultsAdapter {
        return SearchResultsAdapter()
    }
}
