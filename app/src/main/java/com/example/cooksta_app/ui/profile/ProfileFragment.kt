package com.example.cooksta_app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.example.cooksta_app.BuildConfig
import com.example.cooksta_app.LoginActivity
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.FragmentProfileBinding
import com.example.cooksta_app.network.RetrofitClient
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private fun handleLogout() {
        // TODO: Handle logout
        val sharedPreferences = requireActivity().getSharedPreferences("AppPreferences", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean("isLoggedIn", false)
            apply()
        }
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        val userPostsGrid = PostsGrid()
        val savedPostsGrid = PostsGrid()
        val adapter = ProfilePagerAdapter(this, listOf(userPostsGrid, savedPostsGrid))
        binding.profileViewPager.adapter = adapter

        TabLayoutMediator(binding.postsTabLayout, binding.profileViewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Eigene Rezepte"
                1 -> "Gespeichert"
                else -> ""
            }
        }.attach()

        binding.logoutButton.setOnClickListener {
            handleLogout()
        }
        binding.editButton.setOnClickListener {
            findNavController().navigate(R.id.action_to_edit_profile)
        }

        lifecycleScope.launch {
            try {
                val user = RetrofitClient.apiService.getMe()
                binding.username.text = user.username
                binding.biography.text = user.biography
                val imageUrl = "${BuildConfig.API_BASE_URL}/users/${user.id}/avatar"
                Glide.with(requireParentFragment()).load(imageUrl).placeholder(R.drawable.ic_launcher).into(binding.profileImage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ProfilePagerAdapter(fragment: Fragment, private val fragments: List<Fragment>) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}
