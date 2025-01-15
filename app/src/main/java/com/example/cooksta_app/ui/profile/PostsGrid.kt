package com.example.cooksta_app.ui.profile

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksta_app.R
import com.example.cooksta_app.ui.shared.RecyclerViewFragment

data class Post(
    val thumbnailResource: Int
)

class PostsGrid(
    private val posts: List<Post>
) : RecyclerViewFragment<PostsGridAdapter>() {
    private val spanCount = 3

    override fun provideAdapter(): PostsGridAdapter {
        return PostsGridAdapter(posts)
    }

    override fun provideLayoutManager(): RecyclerView.LayoutManager {
        return GridLayoutManager(context, spanCount)
    }

    override fun provideDecoration(): RecyclerView.ItemDecoration {
        return object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val totalWidth = parent.width
                val itemWidth = totalWidth / spanCount
                view.layoutParams = view.layoutParams.apply {
                    width = itemWidth
                    height = itemWidth
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
