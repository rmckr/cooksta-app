package com.example.cooksta_app.ui.profile

import android.graphics.Rect
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksta_app.R
import com.example.cooksta_app.ui.shared.RecyclerViewFragment

data class Post(
    val thumbnailResource: Int
)

class PostsGrid : RecyclerViewFragment<PostsGridAdapter>() {
    private val posts: List<Post> = listOf(
        Post(R.drawable.ic_cake),
        Post(R.drawable.ic_soup),
        Post(R.drawable.ic_cake),
        Post(R.drawable.ic_apple),
        Post(R.drawable.ic_lunch),
        Post(R.drawable.ic_cake),
        Post(R.drawable.ic_soup),
        Post(R.drawable.ic_cake),
        Post(R.drawable.ic_apple),
        Post(R.drawable.ic_lunch),
        Post(R.drawable.ic_cake),
        Post(R.drawable.ic_soup),
        Post(R.drawable.ic_cake),
        Post(R.drawable.ic_apple),
        Post(R.drawable.ic_lunch),
    )
    private val spanCount = 3
    private val spacing = 4

    override fun provideAdapter(): PostsGridAdapter {
        return PostsGridAdapter(posts)
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
    private val posts: List<Post>
) : RecyclerView.Adapter<PostsGridAdapter.PostsGridViewHolder>() {
    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsGridViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post_grid, parent, false)
        return PostsGridViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostsGridViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    inner class PostsGridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.findViewById<ImageView>(R.id.postGridImage)

        fun bind(post: Post) {
            imageView.setImageResource(post.thumbnailResource)
        }
    }
}
