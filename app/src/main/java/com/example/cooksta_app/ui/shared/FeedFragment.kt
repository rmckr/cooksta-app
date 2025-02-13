package com.example.cooksta_app.ui.shared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.cooksta_app.databinding.FragmentFeedBinding
import com.example.cooksta_app.model.Recipe
import kotlinx.coroutines.launch

abstract class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private var posts: List<Recipe> = emptyList()

    abstract suspend fun providePosts(): List<Recipe>

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

        val feedAdapter = FeedAdapter()
        binding.homeViewPager.adapter = feedAdapter
        binding.homeViewPager.orientation = ViewPager2.ORIENTATION_VERTICAL

        viewLifecycleOwner.lifecycleScope.launch {
            posts = providePosts()
            feedAdapter.notifyItemRangeInserted(0, posts.size)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setViewPagerInput(state: Boolean) {
        binding.homeViewPager.isUserInputEnabled = state
    }

    inner class FeedAdapter : FragmentStateAdapter(this) {
        override fun createFragment(position: Int): Fragment {
            val recipe = posts[position]
            return FeedPostFragment.newInstance(recipe)
        }

        override fun getItemCount(): Int = posts.size
    }
}

