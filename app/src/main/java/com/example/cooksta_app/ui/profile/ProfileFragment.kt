package com.example.cooksta_app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.cooksta_app.BuildConfig
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.FragmentEditProfileBinding
import com.example.cooksta_app.databinding.FragmentProfileBinding
import com.example.cooksta_app.network.RetrofitClient
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private fun handleLogout() {
        // TODO: Handle logout
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

        binding.logoutButton.setOnClickListener {
            handleLogout()
        }
        binding.editButton.setOnClickListener {
            findNavController().navigate(R.id.action_to_edit_profile)
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

class EditProfileFragment : DialogFragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private fun saveProfileData() {
        // TODO: Save profile data
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.saveProfile -> {
                    saveProfileData()
                    findNavController().navigateUp()
                    true
                }
                else -> false
            }
        }
        binding.saveProfileButton.setOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(null)
            setWindowAnimations(android.R.style.Animation_Translucent)
            attributes = attributes.apply { dimAmount = 0f }
        }
    }
}
