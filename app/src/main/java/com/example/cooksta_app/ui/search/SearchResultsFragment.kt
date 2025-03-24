package com.example.cooksta_app.ui.search

import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.ItemSearchResultBinding
import com.example.cooksta_app.model.Recipe
import com.example.cooksta_app.ui.shared.RecyclerViewFragment
import com.example.cooksta_app.ui.shared.feed.FeedFragment
import com.example.cooksta_app.utils.MediaCacheManager
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchResultsFragment : RecyclerViewFragment<SearchResultsFragment.Adapter>() {

    private val mediaCacheManager by lazy {
        MediaCacheManager(requireActivity())
    }

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

    override fun provideAdapter(): Adapter {
        return Adapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (recipes.isEmpty()) {
            val emptyView = MaterialTextView(requireContext()).apply {
                text = "Keine Ergebnisse"
                gravity = Gravity.CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
                setPadding(padding)
            }
            return emptyView
        } else {
           return super.onCreateView(inflater, container, savedInstanceState)
        }
    }

    inner class Adapter : RecyclerView.Adapter<Adapter.SearchResultItemViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultItemViewHolder {
            val binding = ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SearchResultItemViewHolder(binding)
        }

        override fun onBindViewHolder(holder: SearchResultItemViewHolder, position: Int) {
            val recipe = recipes[position]
            holder.bind(recipe)
        }

        override fun getItemCount(): Int {
            return recipes.size
        }

        inner class SearchResultItemViewHolder(private val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(recipe: Recipe) {
                binding.searchRecipeName.text = recipe.title
                lifecycleScope.launch {
                    val bitmap = mediaCacheManager.getThumbnailBitmap(recipe.id)
                    if (bitmap != null) {
                        withContext(Dispatchers.Main) { binding.searchRecipePicture.load(bitmap) }
                    }
                }

                binding.searchResultCard.setOnClickListener {
                    findNavController(binding.root).navigate(
                        R.id.action_navigation_search_to_navigation_feed,
                        Bundle().apply {
                            putStringArray(FeedFragment.ARG_RECIPE_IDS, recipes.map { it.id }.toTypedArray())
                            putInt(FeedFragment.ARG_POSITION, bindingAdapterPosition)
                        }
                    )
                }
            }
        }
    }
}
