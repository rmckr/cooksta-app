package com.example.cooksta_app.ui.create

import android.animation.ObjectAnimator
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.FragmentCreateBinding
import com.example.cooksta_app.model.Category
import com.example.cooksta_app.model.RecipeBody
import com.example.cooksta_app.network.RetrofitClient
import com.example.cooksta_app.ui.profile.ProfileFragment
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CreateFragment : Fragment() {

    private val categoryChipMap = mapOf(
        R.id.categoryChipAsian to Category.ASIAN,
        R.id.categoryChipBaking to Category.BAKING,
        R.id.categoryChipDessert to Category.DESSERT,
        R.id.categoryChipDrink to Category.DRINK,
        R.id.categoryChipMain to Category.MAIN,
        R.id.categoryChipItalian to Category.ITALIAN,
        R.id.categoryChipQuick to Category.QUICK,
        R.id.categoryChipSoup to Category.SOUP,
        R.id.categoryChipVegetarian to Category.VEGETARIAN,
    )

    private var _binding: FragmentCreateBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateViewModel by lazy {
        ViewModelProvider(requireActivity())[CreateViewModel::class.java]
    }

    private var selectedImageUri: Uri? = null
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedImageUri = uri
        selectedVideoUri = null
        uri?.let {
            Glide.with(requireActivity()).load(uri).into(binding.imagePreview)
        }
        Glide.with(requireActivity()).clear(binding.videoPreview)
    }

    private var selectedVideoUri: Uri? = null
    private val pickVideoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedVideoUri = uri
        selectedImageUri = null
        uri?.let {
            Glide.with(requireActivity()).asBitmap().load(uri).into(binding.videoPreview)
        }
        Glide.with(requireActivity()).clear(binding.imagePreview)
    }

    private fun triggerRippleEffect(view: View) {
        view.isPressed = true
        view.invalidate()
        view.postDelayed({
            view.isPressed = false
            view.invalidate()
        }, 1000)
    }

    private fun shakeView(view: View) {
        ObjectAnimator.ofFloat(view, "translationX", 0f, 10f, -10f, 5f, -5f, 0f).apply {
            duration = 300
            start()
        }
    }

    private suspend fun showMissingAmountDialog(): Boolean {
        return suspendCoroutine { continuation ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.title_dialog_ingredients))
                .setMessage(getString(R.string.message_dialog_ingredients))
                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                    dialog.dismiss()
                    continuation.resume(true)
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                    continuation.resume(false)
                }
                .show()
        }
    }

    private suspend fun validateInputs(): Boolean {
        var isValid = true

        // Title
        if (binding.recipeTitleInput.text.isNullOrBlank()) {
            binding.recipeTitleLayout.error = getString(R.string.error_title_required)
            binding.recipeTitleInput.requestFocus()
            isValid = false
        } else {
            binding.recipeTitleLayout.error = null
        }

        // Description
        if (binding.recipeDescriptionInput.text.isNullOrBlank()) {
            binding.recipeDescriptionLayout.error = getString(R.string.error_description_required)
            if (isValid) binding.recipeDescriptionInput.requestFocus()
            isValid = false
        } else {
            binding.recipeDescriptionLayout.error = null
        }

        if (!isValid) { return false }

        // Ingredients
        val ingredientViewModel: CreateViewModel by activityViewModels()

        if (ingredientViewModel.ingredientSelectionCount == 0) {
            binding.emptyIngredientSelectionView.visibility = View.VISIBLE
            triggerRippleEffect(binding.ingredients)
            shakeView(binding.ingredients)
            isValid = false
        } else {
            binding.emptyIngredientSelectionView.visibility = View.GONE
        }

        val ingredientSelectionListFragment =
            childFragmentManager.findFragmentById(R.id.ingredientSelectionList) as? IngredientSelectionListFragment

        if (ingredientSelectionListFragment != null) {
            with(ingredientSelectionListFragment) {
                var firstInvalidTextLayout: View? = null
                var hasNullIngredient = false
                val ingredientNames = mutableSetOf<String>()

                for (i in 0 until adapter.itemCount) {
                    val ingredient = adapter.currentList[i]

                    val name = ingredient.name?.trim()
                    val amount = ingredient.amount

                    val viewHolder = recyclerView.findViewHolderForAdapterPosition(i) as? IngredientSelectionListFragment.Adapter.IngredientViewHolder
                    if (name.isNullOrBlank()) {
                        isValid = false
                        viewHolder?.ingredientNameLayout?.error = getString(R.string.error_choose_ingredient)
                        if (firstInvalidTextLayout === null) firstInvalidTextLayout = viewHolder?.ingredientNameLayout
                    } else if (!ingredientNames.add(name)) {
                        isValid = false
                        viewHolder?.ingredientNameLayout?.error = getString(R.string.error_ingredients)
                        if (firstInvalidTextLayout === null) firstInvalidTextLayout = viewHolder?.ingredientNameLayout
                    } else {
                        viewHolder?.ingredientNameLayout?.error = null
                    }

                    if (amount == null) {
                        hasNullIngredient = true
                    }
                }

                if (hasNullIngredient) {
                    val dialogResult = showMissingAmountDialog()
                    if (!dialogResult) {
                        isValid = false
                    }
                }
            }
        }

        if (!isValid) { return false }

        // Category
        if (binding.chipGroup.checkedChipIds.isEmpty()) {
            binding.emptyCategorySelectionView.visibility = View.VISIBLE
            triggerRippleEffect(binding.categories)
            shakeView(binding.categories)
            isValid = false
        } else {
            binding.emptyCategorySelectionView.visibility = View.GONE
        }

        if(!isValid) { return false }

        // Media
        if (selectedImageUri == null && selectedVideoUri == null) {
            binding.emptyMediaSelectionView.visibility = View.VISIBLE
            triggerRippleEffect(binding.media)
            shakeView(binding.media)
            isValid = false
        } else {
            binding.imagePreview.setBackgroundResource(0)
            binding.emptyMediaSelectionView.visibility = View.GONE
        }

        return isValid
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.portions.collect { portions ->
                binding.recipePortionsSelector.setCurrentValue(portions)
            }
        }
        binding.recipePortionsSelector.setOnValueChangeListener {
            viewModel.setPortions(it)
        }

        binding.addIngredientButton.setOnClickListener {
            val ingredientSelectionListFragment = childFragmentManager.findFragmentById(R.id.ingredientSelectionList) as? IngredientSelectionListFragment
            ingredientSelectionListFragment?.createIngredient()
        }

        binding.recipeSaveButton.setOnClickListener {
            val originalDrawable = binding.recipeSaveButton.icon
            binding.recipeSaveButton.icon = CircularProgressDrawable(requireContext()).apply {
                strokeWidth = 4f
                start()
            }
            lifecycleScope.launch {
                if (validateInputs()) {
                    val postResponse = RetrofitClient.apiService.postRecipe(
                        RecipeBody(
                            title = binding.recipeTitleInput.text.toString(),
                            description = binding.recipeDescriptionInput.text.toString(),
                            portions = viewModel.portions.value,
                            categories = binding.chipGroup.checkedChipIds.mapNotNull { categoryChipMap[it] },
                            ingredients = viewModel.ingredientSelection.value.associate { it.name!! to it.amount },
                            isPrivate = !binding.isPublicSwitch.isChecked,
                        )
                    )
                    if (postResponse.isSuccessful && postResponse.body()?.id != null) {
                        val uri = selectedVideoUri ?: selectedImageUri ?: throw IllegalStateException("No media selected")
                        val mediaType = if (selectedVideoUri != null) "video" else "image"
                        val inputStream = requireContext().contentResolver.openInputStream(uri)!!
                        val mimeType = requireContext().contentResolver.getType(uri) ?: "$mediaType/*"
                        val requestBody = inputStream.use { stream ->
                            val bytes = stream.readBytes()
                            bytes.toRequestBody(mimeType.toMediaTypeOrNull(), 0, bytes.size)
                        }
                        val part = MultipartBody.Part.createFormData(mediaType, mediaType, requestBody)
                        val mediaResponse = RetrofitClient.apiService.putRecipeMedia(postResponse.body()!!.id, part)
                        if (mediaResponse.isSuccessful) {
                            findNavController().navigate(
                                R.id.navigation_profile,
                                Bundle().apply {
                                    putString(ProfileFragment.ARG_RECIPE_ID, postResponse.body()!!.id)
                                }
                            )
                        } else {
                            Toast.makeText(requireContext(), "Fehler beim Hochladen der Datei", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Fehler beim Erstellen des Rezepts", Toast.LENGTH_LONG).show()
                    }
                }
                binding.recipeSaveButton.icon = originalDrawable
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}