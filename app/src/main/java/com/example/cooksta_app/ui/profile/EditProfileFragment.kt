package com.example.cooksta_app.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.cooksta_app.BuildConfig
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.FragmentEditProfileBinding
import com.example.cooksta_app.model.PartialUser
import com.example.cooksta_app.network.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by lazy {
        ViewModelProvider(this)[ProfileViewModel::class.java]
    }

    private var selectedImageUri: Uri? = null
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            Glide.with(requireActivity()).load(uri).into(binding.editProfileImage)
            selectedImageUri = uri
        }
    }

    private fun saveProfileData() {
        val username = binding.editUsernameInput.text.toString()
        val biography = binding.editBiographyInput.text.toString()
        val password = binding.editPasswordInput.text.toString()

        lifecycleScope.launch {
            val patchResponse = RetrofitClient.apiService.patchUser("me", PartialUser(
                username = username,
                biography = biography,
                password = if (password != "........") password else null)
            )

            var uploadResponse: Response<Void>? = null
            if (selectedImageUri !== null) {
                val inputStream = requireContext().contentResolver.openInputStream(selectedImageUri!!)!!
                val mimeType = requireContext().contentResolver.getType(selectedImageUri!!) ?: "image/*"
                val requestBody = inputStream.use { stream ->
                    val bytes = stream.readBytes()
                    bytes.toRequestBody(mimeType.toMediaTypeOrNull(), 0, bytes.size)
                }
                val part = MultipartBody.Part.createFormData("avatar", "avatar", requestBody)
                uploadResponse = RetrofitClient.apiService.putAvatar(part)
            }

            if (patchResponse.isSuccessful && uploadResponse?.isSuccessful != false) {
                findNavController().popBackStack()
            } else {
                Toast.makeText(context, "Fehler beim Speichern", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadUser("me")

        lifecycleScope.launch {
            viewModel.user.collect { user ->
                binding.editUsernameInput.setText(user?.username)
                binding.editBiographyInput.setText(user?.biography)
                binding.editPasswordInput.setText(if (user != null) "........" else null)

                if (user !== null) {
                    val imageUrl = "${BuildConfig.API_BASE_URL}/users/${user.id}/avatar"
                    Glide.with(requireActivity())
                        .load(imageUrl)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .placeholder(R.drawable.ic_launcher)
                        .into(binding.editProfileImage)
                }
            }
        }

        binding.editProfileImageView.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.saveProfile -> {
                    saveProfileData()
                    true
                }
                else -> false
            }
        }
        binding.saveProfileButton.setOnClickListener {
            saveProfileData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
