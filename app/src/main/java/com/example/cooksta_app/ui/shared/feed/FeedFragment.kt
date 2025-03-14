package com.example.cooksta_app.ui.shared.feed

import android.os.Bundle
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
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.FragmentFeedBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class FeedFragment : Fragment() {

    companion object {
        const val ARG_RECIPE_IDS = "recipeIds"
        const val ARG_IS_HOME_FEED = "isHomeFeed"
        const val ARG_POSITION = "position"

        // TODO: Think about deleting this
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
    private val position: Int by lazy {
        arguments?.getInt(ARG_POSITION) ?: 0
    }

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    fun setViewPagerInput(state: Boolean) {
        binding.feedViewPager.isUserInputEnabled = state
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }

            WindowInsetsCompat.CONSUMED
        }

        if (isHomeFeed) {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                activity?.finish()
            }
            requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).menu.findItem(R.id.navigation_home).isChecked = true
        } else {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                findNavController().popBackStack()
            }
            binding.feedBackButton.visibility = View.VISIBLE
            binding.feedBackButton.setOnClickListener {
                findNavController().popBackStack()
            }
        }

        val feedAdapter = FeedAdapter()
        binding.feedViewPager.adapter = feedAdapter
        binding.feedViewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        binding.feedViewPager.offscreenPageLimit = 1

        if (position > 0) {
            binding.feedViewPager.setCurrentItem(position, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class FeedAdapter : FragmentStateAdapter(this) {
        override fun createFragment(position: Int): Fragment {
            val recipeId = recipeIds[position]
            return FeedPostFragment.newInstance(recipeId)
        }

        override fun getItemCount(): Int = recipeIds.size
    }
}

