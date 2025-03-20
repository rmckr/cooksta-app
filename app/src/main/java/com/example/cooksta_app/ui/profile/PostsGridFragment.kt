package com.example.cooksta_app.ui.profile

import android.graphics.Rect
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksta_app.R
import com.example.cooksta_app.ui.shared.RecyclerViewFragment
import com.example.cooksta_app.ui.shared.feed.FeedFragment
import com.example.cooksta_app.ui.shared.skeleton.SkeletonImageView
import com.example.cooksta_app.utils.MediaCacheManager
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostsGridFragment : RecyclerViewFragment<PostsGridFragment.Adapter>() {

    private val mediaCacheManager by lazy {
        MediaCacheManager(requireActivity())
    }

    private val recipes: ArrayList<String> by lazy {
        arguments?.getStringArrayList(ARG_RECIPE_IDS) ?: throw IllegalStateException("$ARG_RECIPE_IDS argument is missing")
    }

    val spanCount by lazy {
        if (resources.displayMetrics.widthPixels / resources.displayMetrics.density >= 820) {
            6
        } else {
            3
        }
    }

    private val spacing = 4

    companion object {
        private const val ARG_RECIPE_IDS = "recipeIds"

        fun newInstance(recipes: List<String>): PostsGridFragment {
            val args = Bundle()
            args.putStringArrayList(ARG_RECIPE_IDS, ArrayList(recipes))
            val fragment = PostsGridFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (recipes.isEmpty()) {
            val emptyView = MaterialTextView(requireContext()).apply {
                text = getString(R.string.no_results)
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

    override fun provideAdapter(): Adapter {
        return Adapter(recipes)
    }

    override fun provideLayoutManager(): RecyclerView.LayoutManager {
        return GridLayoutManager(context, spanCount)
    }

    override fun provideDecoration(): RecyclerView.ItemDecoration {
        val pixelSpacing = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            spacing.toFloat(),
            resources.displayMetrics
        ).toInt()
        return object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                val column = position % spanCount
                outRect.left = pixelSpacing * column / spanCount
                outRect.right = pixelSpacing - (column + 1) * pixelSpacing / spanCount
                outRect.top = if (position < spanCount) 0 else pixelSpacing
                outRect.bottom = 0

                val itemSize = (parent.width - pixelSpacing * (spanCount - 1)) / spanCount

                view.layoutParams = view.layoutParams.apply {
                    width = itemSize
                    height = itemSize
                }
            }
        }
    }

    inner class Adapter(private val recipeIds: List<String>) : RecyclerView.Adapter<Adapter.PostsGridItemViewHolder>() {
        override fun getItemCount(): Int {
            return recipeIds.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsGridItemViewHolder {
            val view = SkeletonImageView(parent.context, null).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            return PostsGridItemViewHolder(view)
        }

        override fun onBindViewHolder(holder: PostsGridItemViewHolder, position: Int) {
            val recipeId = recipeIds[position]
            holder.bind(recipeId)
        }

        inner class PostsGridItemViewHolder(private val imageView: SkeletonImageView) : RecyclerView.ViewHolder(imageView) {
            fun bind(recipeId: String) {
                lifecycleScope.launch {
                    val bitmap = mediaCacheManager.getThumbnailBitmap(recipeId)
                    if (bitmap !== null) {
                        withContext(Dispatchers.Main) { imageView.load(bitmap) }
                    }
                }
                imageView.setOnClickListener {
                    findNavController(imageView).navigate(
                        R.id.action_navigation_profile_to_navigation_feed,
                        Bundle().apply {
                            putStringArray(FeedFragment.ARG_RECIPE_IDS, recipeIds.toTypedArray())
                            putInt(FeedFragment.ARG_POSITION, bindingAdapterPosition)
                        }
                    )
                }
            }
        }
    }
}
