package com.example.cooksta_app.ui.create

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.FragmentCreateBinding
import com.google.android.material.chip.Chip
import com.google.android.material.shape.MaterialShapeDrawable

class CreateFragment : Fragment() {

    private var _binding: FragmentCreateBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateViewModel by lazy {
        ViewModelProvider(requireActivity())[CreateViewModel::class.java]
    }

    private var selectedImageUri: Uri? = null
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            Glide.with(this).load(uri).into(binding.imagePreview)
        }
    }

    private var selectedVideoUri: Uri? = null
    private val pickVideoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedVideoUri = uri
        uri?.let {
            Glide.with(this).asBitmap().load(uri).into(binding.videoPreview)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateBinding.inflate(inflater, container, false)

        binding.createAppBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(context)

        val maxSelection = 3
        var selectedChipCount = 0
        for (i in 0 until binding.chipGroup.childCount) {
            val chip = binding.chipGroup.getChildAt(i) as Chip
            chip.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    if (selectedChipCount >= maxSelection) {
                        buttonView.isChecked = false
                    } else {
                        selectedChipCount++
                    }
                } else {
                    selectedChipCount--
                }
            }
        }

        viewModel.portions.observe(viewLifecycleOwner) { value ->
            binding.recipePortionsSelector.setValue(value)
        }
        binding.recipePortionsSelector.setOnValueChangeListener {
            viewModel.setPortions(it)
        }

        binding.addIngredientButton.setOnClickListener {
            val ingredientSelectionListFragment = childFragmentManager.findFragmentById(R.id.ingredientSelectionList) as? IngredientSelectionListFragment
            ingredientSelectionListFragment?.let {
                it.createIngredient()
            }
        }

        binding.selectImageButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        binding.imagePreview.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        binding.selectVideoButton.setOnClickListener {
            pickVideoLauncher.launch("video/*")
        }
        binding.videoPreview.setOnClickListener {
            pickVideoLauncher.launch("video/*")

        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}