package com.example.cooksta_app.ui.profile

import android.graphics.Rect
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksta_app.R
import com.example.cooksta_app.ui.shared.FeedFragment
import com.example.cooksta_app.ui.shared.RecyclerViewFragment
import com.example.cooksta_app.ui.shared.Utilities
import com.example.cooksta_app.ui.shared.skeleton.SkeletonImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostsGrid : RecyclerViewFragment<PostsGridAdapter>() {
    private val recipes: ArrayList<String> by lazy {
        arguments?.getStringArrayList(ARG_RECIPE_IDS) ?: throw IllegalStateException("recipeIds argument is missing")
    }

    companion object {
        private const val ARG_RECIPE_IDS = "recipeIds"

        fun newInstance(recipes: List<String>): PostsGrid {
            val args = Bundle()
            args.putStringArrayList(ARG_RECIPE_IDS, ArrayList(recipes))
            val fragment = PostsGrid()
            fragment.arguments = args
            return fragment
        }
    }

    private val spanCount = 3
    private val spacing = 4

    override fun provideAdapter(): PostsGridAdapter {
        return PostsGridAdapter(recipes)
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
}

class PostsGridAdapter(
    private val recipeIds: List<String>
) : RecyclerView.Adapter<PostsGridAdapter.PostsGridViewHolder>() {
    override fun getItemCount(): Int {
        return recipeIds.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsGridViewHolder {
        val view = SkeletonImageView(parent.context, null).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        return PostsGridViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostsGridViewHolder, position: Int) {
        val recipeId = recipeIds[position]
        holder.bind(recipeId)
    }

    inner class PostsGridViewHolder(private val imageView: SkeletonImageView) : RecyclerView.ViewHolder(imageView) {
        fun bind(recipeId: String) {
            CoroutineScope(Dispatchers.IO).launch {
                val bitmap = Utilities.fetchVideoThumbnail(imageView.context, recipeId)
                if (bitmap !== null) {
                    withContext(Dispatchers.Main) {
                        imageView.load(bitmap)
                    }
                }
            }
            imageView.setOnClickListener {
                findNavController(imageView).navigate(
                    R.id.action_navigation_profile_to_navigation_feed,
                    Bundle().apply {
                        putStringArray(FeedFragment.ARG_RECIPE_IDS, recipeIds.toTypedArray())
                        putInt(FeedFragment.ARG_POSITION, adapterPosition)
                    }
                )
            }
        }
    }
}
