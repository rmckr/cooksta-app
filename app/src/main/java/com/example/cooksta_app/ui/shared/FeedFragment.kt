package com.example.cooksta_app.ui.shared

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.cooksta_app.databinding.FragmentFeedBinding

class FeedFragment : Fragment() {

    companion object {
        const val ARG_RECIPE_IDS = "recipeIds"
        const val ARG_IS_HOME_FEED = "isHomeFeed"

        fun newInstance(recipeIds: List<String>): FeedFragment = FeedFragment().apply {
            arguments = Bundle().apply {
                putStringArray(ARG_RECIPE_IDS, recipeIds.toTypedArray())
            }
        }
    }

    private val recipeIds: List<String> by lazy {
        arguments?.getStringArray(ARG_RECIPE_IDS)?.toList() ?: throw IllegalStateException("$ARG_RECIPE_IDS argument is missing")
    }
    private val isHomeFeed: Boolean by lazy {
        arguments?.getBoolean(ARG_IS_HOME_FEED, false) ?: false
    }

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

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

        Log.d("TEST", "onCreateView: $isHomeFeed")
        if (isHomeFeed) {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                activity?.finish()
            }
        } else {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                findNavController().popBackStack()
            }
        }

        val feedAdapter = FeedAdapter()
        binding.feedViewPager.adapter = feedAdapter
        binding.feedViewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        binding.feedViewPager.offscreenPageLimit = 1

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setViewPagerInput(state: Boolean) {
        binding.feedViewPager.isUserInputEnabled = state
    }

    inner class FeedAdapter : FragmentStateAdapter(this) {
        override fun createFragment(position: Int): Fragment {
            val recipeId = recipeIds[position]
            return FeedPostFragment.newInstance(position, recipeId)
        }

        override fun getItemCount(): Int = recipeIds.size
    }
}

