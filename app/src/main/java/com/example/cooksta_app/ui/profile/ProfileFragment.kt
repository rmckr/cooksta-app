package com.example.cooksta_app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.cooksta_app.BuildConfig
import com.example.cooksta_app.EditProfileActivity
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.FragmentProfileBinding
import com.example.cooksta_app.network.RetrofitClient
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

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

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.editProfile -> {
                    val intent = Intent(requireContext(), EditProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        val userId = "67783a1643964ac750c1c18c" // Replace with actual user ID logic
        val imageUrl = "${BuildConfig.API_BASE_URL}/users/${userId}/avatar"

        binding.profileImage.load(imageUrl)
        lifecycleScope.launch {
            try {
                val user = RetrofitClient.apiService.getUserInfo(userId)
                binding.username.text = user.username
                binding.biography.text = user.biography
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
