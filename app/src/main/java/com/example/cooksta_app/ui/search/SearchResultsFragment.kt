package com.example.cooksta_app.ui.search

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cooksta_app.databinding.ItemSearchResultBinding
import com.example.cooksta_app.model.Recipe
import com.example.cooksta_app.network.RetrofitClient
import com.example.cooksta_app.ui.shared.RecyclerViewFragment
import com.example.cooksta_app.ui.shared.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    inner class SearchResultsAdapter : RecyclerView.Adapter<SearchResultViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
            // TODO: Create layout
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
    }

    override fun provideAdapter(): SearchResultsAdapter {
        return SearchResultsAdapter()
    }
}

class SearchResultViewHolder(private val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(recipe: Recipe) {
        with(binding) {
            searchRecipeName.text = recipe.title
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.apiService.getRecipeMedia(recipe.id)
                    if (response.isSuccessful) {
                        val mediaType = response.headers()["Content-Type"]
                        val mediaStream = response.body()?.byteStream()

                        if (mediaType?.contains("image") == true) {
                            CoroutineScope(Dispatchers.Main).launch {
                                Glide.with(serachRecipePicture.context)
                                    .load(mediaStream)
                                    .into(serachRecipePicture)
                            }
                        } else if (mediaType?.contains("video") == true) {
                            val bitmap = Utilities.getVideoThumbnail(mediaStream)
                            CoroutineScope(Dispatchers.Main).launch {
                                serachRecipePicture.setImageBitmap(bitmap)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SearchResultViewHolder", "Fehler beim Laden des Media", e)
                }
            }
        }
    }


}
