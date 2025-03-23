package com.example.cooksta_app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.cooksta_app.BuildConfig
import com.example.cooksta_app.LoginActivity
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.FragmentProfileBinding
import com.example.cooksta_app.network.RetrofitClient
import com.example.cooksta_app.ui.shared.LoadingFragment
import com.example.cooksta_app.ui.shared.feed.FeedFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by lazy {
        ViewModelProvider(this)[ProfileViewModel::class.java]
    }

    private val userId: String by lazy {
        arguments?.getString(ARG_USER_ID) ?: "me"
    }

    private val recipeId: String? by lazy {
        arguments?.getString(ARG_RECIPE_ID)
    }

    companion object {
        const val ARG_USER_ID = "userId"
        const val ARG_RECIPE_ID = "recipeId"

        fun newInstance(userId: String): ProfileFragment {
            val args = Bundle()
            args.putString(ARG_USER_ID, userId)
            val fragment = ProfileFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private fun handleLogout() {
        lifecycleScope.launch {
            val logoutSuccessful = RetrofitClient.logout()
            if (logoutSuccessful) {
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), "Logout failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.profileHeader) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                topMargin = insets.top
                rightMargin = insets.right
            }

            WindowInsetsCompat.CONSUMED
        }

        viewModel.loadUserData(userId)

        lifecycleScope.launch {
            viewModel.userState.collect { userState ->
                when (userState) {
                    is ProfileViewModel.UserState.Success -> {
                        binding.username.load(userState.data.username)
                        binding.biography.load(userState.data.biography)
                        val imageUrl = "${BuildConfig.API_BASE_URL}/users/${userState.data.id}/avatar"
                        Glide.with(requireActivity())
                            .load(imageUrl)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .placeholder(R.drawable.ic_launcher)
                            .into(binding.profileImage)
                    }
                    is ProfileViewModel.UserState.Error -> {
                        Toast.makeText(requireContext(), userState.message, Toast.LENGTH_SHORT).show()
                        binding.username.unload()
                        binding.biography.unload()
                        Glide.with(requireActivity()).load(R.drawable.ic_launcher).into(binding.profileImage)
                    }
                    else -> {
                        binding.username.unload()
                        binding.biography.unload()
                        Glide.with(requireActivity()).load(R.drawable.ic_launcher).into(binding.profileImage)
                    }
                }
            }
        }
        lifecycleScope.launch {
            combine(viewModel.userState, viewModel.userRecipesState) { userState, userRecipesState ->
                when {
                    userState is ProfileViewModel.UserState.Success && userRecipesState is ProfileViewModel.UserRecipesState.Success -> {
                        listOf(userRecipesState.data.map { it.id }, userState.data.favorites)
                    }
                    userState is ProfileViewModel.UserState.Error -> {
                        Toast.makeText(requireContext(), userState.message, Toast.LENGTH_SHORT).show()
                        null
                    }
                    userRecipesState is ProfileViewModel.UserRecipesState.Error -> {
                        Toast.makeText(requireContext(), userRecipesState.message, Toast.LENGTH_SHORT).show()
                        null
                    }
                    else -> {
                        null
                    }
                }
            }.collect { recipeLists ->
                if (recipeLists != null) {
                    if (recipeId != null && recipeLists[0].isEmpty() == false) {
                        val index = recipeLists[0].indexOf(recipeId)
                        if (index != -1) {
                            findNavController().navigate(
                                R.id.navigation_feed,
                                Bundle().apply {
                                    putStringArray(FeedFragment.ARG_RECIPE_IDS, recipeLists[0].toTypedArray())
                                    putInt(FeedFragment.ARG_POSITION, index)
                                }
                            )
                        }
                    } else {
                        binding.profileViewPager.adapter = PagerAdapter(this@ProfileFragment, recipeLists)
                        TabLayoutMediator(binding.postsTabLayout, binding.profileViewPager) { tab, position ->
                            tab.text = when (position) {
                                0 -> getString(R.string.own_recipes)
                                1 -> if (userId == "me") getString(R.string.saved) else null
                                else -> null
                            }
                        }.attach()
                    }
                }
            }
        }

        if (userId != "me") {
            binding.logoutButton.visibility = View.GONE
            binding.editButton.visibility = View.GONE
        }

        binding.logoutButton.setOnClickListener {
            handleLogout()
        }
        binding.editButton.setOnClickListener {
            findNavController().navigate(R.id.action_to_edit_profile)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class PagerAdapter(fragment: Fragment, private val recipeLists: List<List<String>?>) : FragmentStateAdapter(fragment) {
        override fun getItemCount() = if (userId == "me") 2 else 1

        override fun createFragment(position: Int): Fragment {
            val recipeIds = recipeLists[position]
            return if (recipeIds !== null) PostsGridFragment.newInstance(recipeIds) else LoadingFragment()
        }
    }
}
