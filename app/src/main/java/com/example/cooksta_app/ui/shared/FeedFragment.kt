package com.example.cooksta_app.ui.shared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.cooksta_app.databinding.FragmentFeedBinding

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private val recipeIds: List<String> by lazy {
        arguments?.getStringArrayList(ARG_RECIPE_IDS)?.toList() ?: throw IllegalStateException("$ARG_RECIPE_IDS argument is missing")
    }

    companion object {
        private const val ARG_RECIPE_IDS = "recipeIds"

        fun newInstance(recipeIds: List<String>): FeedFragment = FeedFragment().apply {
            arguments = Bundle().apply {
                putStringArrayList(ARG_RECIPE_IDS, ArrayList(recipeIds))
            }
        }
    }

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
        binding.homeViewPager.offscreenPageLimit = 1

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
            val recipeId = recipeIds[position]
            return FeedPostFragment.newInstance(position, recipeId)
        }

        override fun getItemCount(): Int = recipeIds.size
    }
}

