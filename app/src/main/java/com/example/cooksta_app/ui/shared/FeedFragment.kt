package com.example.cooksta_app.ui.shared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.cooksta_app.databinding.FragmentFeedBinding
import com.example.cooksta_app.databinding.ItemPostBinding
import com.example.cooksta_app.model.Post

abstract class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private val posts by lazy { providePosts() }

    abstract fun providePosts(): List<Post>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }

            WindowInsetsCompat.CONSUMED
        }

        binding.homeViewPager.adapter = FeedAdapter()
        binding.homeViewPager.orientation = ViewPager2.ORIENTATION_VERTICAL

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class FeedAdapter : RecyclerView.Adapter<FeedPostViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedPostViewHolder {
            val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return FeedPostViewHolder(binding, this@FeedFragment, this@FeedFragment.binding.homeViewPager)
        }

        override fun getItemCount() = this@FeedFragment.posts.size

        override fun onBindViewHolder(holder: FeedPostViewHolder, position: Int) = holder.bind(this@FeedFragment.posts[position])
    }
}

