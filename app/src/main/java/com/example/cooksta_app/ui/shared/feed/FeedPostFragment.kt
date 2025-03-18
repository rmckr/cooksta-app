package com.example.cooksta_app.ui.shared.feed

import android.animation.ArgbEvaluator
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.example.cooksta_app.BuildConfig
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.DialogAddToShoppingListBinding
import com.example.cooksta_app.databinding.FragmentPostBinding
import com.example.cooksta_app.model.Ingredient
import com.example.cooksta_app.model.PartialUser
import com.example.cooksta_app.model.Recipe
import com.example.cooksta_app.network.RetrofitClient
import com.example.cooksta_app.ui.profile.ProfileFragment
import com.example.cooksta_app.ui.shoppingList.IngredientsListFragment
import com.example.cooksta_app.utils.MediaCacheManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.gpu.BrightnessFilterTransformation
import kotlinx.coroutines.launch
import java.io.File

class FeedPostFragment : Fragment() {

    companion object {
        const val ARG_RECIPE_ID = "recipeId"

        fun newInstance(recipeId: String): FeedPostFragment {
            val fragment = FeedPostFragment()
            val bundle = Bundle()
            bundle.putString(ARG_RECIPE_ID, recipeId)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var _binding: FragmentPostBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FeedPostViewModel by lazy {
        ViewModelProvider(this)[FeedPostViewModel::class.java]
    }

    private val recipeId: String by lazy {
        arguments?.getString(ARG_RECIPE_ID) ?: throw IllegalStateException("$ARG_RECIPE_ID argument is missing")
    }

    private val mediaCacheManager: MediaCacheManager by lazy {
        MediaCacheManager(requireActivity())
    }
    private var exoPlayer: ExoPlayer? = null

    private var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>? = null

    private fun loadImage(file: File) {
        binding.postPlayerView.visibility = View.GONE
        binding.postImage.visibility = View.VISIBLE
        binding.postImageBackdrop.visibility = View.VISIBLE

        Glide.with(requireActivity())
            .load(file)
            .transform(
                MultiTransformation(
                    BrightnessFilterTransformation(-0.25f),
                    BlurTransformation(25, 5)
                )
            )
            .into(binding.postImageBackdrop)

        Glide.with(requireActivity())
            .load(file)
            .into(binding.postImage)
    }

    private fun loadVideo(file: File) {
        binding.postPlayerView.visibility = View.VISIBLE
        binding.postImage.visibility = View.GONE
        binding.postImageBackdrop.visibility = View.GONE

        exoPlayer = ExoPlayer.Builder(requireContext()).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            setMediaItem(MediaItem.fromUri(file.toUri()))
            prepare()
        }

        binding.postPlayerView.player = exoPlayer
        binding.postPlayerView.setOnClickListener {
            exoPlayer!!.playWhenReady = !exoPlayer!!.isPlaying
        }
        exoPlayer!!.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying) {
                    binding.playButton.visibility = View.GONE
                } else {
                    binding.playButton.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun loadMedia() {
        lifecycleScope.launch {
            val file = mediaCacheManager.getMediaFile(recipeId)

            if (file === null) {
                Toast.makeText(requireContext(), getString(R.string.toast_unavailable_media), Toast.LENGTH_SHORT).show()
                return@launch
            }
            when (file.extension) {
                "png" -> loadImage(file)
                "jpg" -> loadImage(file)
                "mp4" -> loadVideo(file)
                else -> Toast.makeText(requireContext(), getString(R.string.toast_unsupported_media), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBottomSheet(recipe: Recipe) {
        binding.bottomSheetTitle.load(recipe.title)

        val minBottomSheetAlpha = 0.25f

        class BottomSheetTouchBehavior : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_DRAGGING, BottomSheetBehavior.STATE_EXPANDED -> {
                        (requireParentFragment() as FeedFragment).setViewPagerInput(false)
                    }

                    else -> {
                        (requireParentFragment() as FeedFragment).setViewPagerInput(true)
                    }
                }
            }

            val evaluator = ArgbEvaluator()
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val interpolatedBackground = evaluator.evaluate(
                    slideOffset.coerceIn(0f, 1f),
                    Color.BLACK,
                    ContextCompat.getColor(requireContext(), R.color.md_theme_surface)
                ) as Int
                binding.bottomSheetLayout.setBackgroundColor(interpolatedBackground)
                binding.bottomSheetLayout.background.alpha =
                    (slideOffset.coerceIn(minBottomSheetAlpha, 1f) * 255).toInt()
                val interpolatedColor = evaluator.evaluate(
                    slideOffset.coerceIn(0f, 1f),
                    Color.WHITE,
                    ContextCompat.getColor(requireContext(), R.color.md_theme_onSurface)
                ) as Int
                binding.bottomSheetTitle.setTextColor(interpolatedColor)
                binding.bottomSheetArrow.imageTintList = ColorStateList.valueOf(interpolatedColor)
                binding.bottomSheetArrow.rotation = slideOffset * 180f
            }
        }
        binding.bottomSheetLayout.setBackgroundColor(Color.BLACK)
        binding.bottomSheetLayout.background.alpha = (minBottomSheetAlpha * 255).toInt()
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior!!.addBottomSheetCallback(BottomSheetTouchBehavior())

        class FragmentAdapter(private val fragments: List<Fragment>) :
            FragmentStateAdapter(this@FeedPostFragment) {
            override fun getItemCount() = fragments.size

            override fun createFragment(position: Int): Fragment {
                return fragments[position]
            }
        }

        val ingredientsListFragment = IngredientsListFragment.withoutCheckboxes()
        ingredientsListFragment.submitIngredients(recipe.ingredients.map { Ingredient(it.key, it.value) })
        binding.bottomSheetViewPager.adapter = FragmentAdapter(
            listOf(
                PostDescriptionFragment.newInstance(recipe.description),
                ingredientsListFragment,
                CommentsFragment()
            )
        )
        TabLayoutMediator(binding.bottomSheetTabLayout, binding.bottomSheetViewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.description)
                1 -> getString(R.string.ingredients)
                2 -> getString(R.string.comments)
                else -> ""
            }
        }.attach()

        viewModel.loadCreator(recipe.creatorId)
        lifecycleScope.launch {
            viewModel.creator.collect { user ->
                if (user != null) {
                    binding.usernameBottomSheet.load(user.username)
                    val imageUrl = "${BuildConfig.API_BASE_URL}/users/${user.id}/avatar"
                    Glide.with(requireActivity()).load(imageUrl).placeholder(R.drawable.ic_launcher).into(binding.profileImageBottomSheet)
                } else {
                    binding.usernameBottomSheet.unload()
                    Glide.with(requireActivity()).load(R.drawable.ic_launcher).into(binding.profileImageBottomSheet)
                }
            }
        }

        binding.bottomSheetProfile.setOnClickListener {
            findNavController().navigate(
                R.id.navigation_profile,
                Bundle().apply {
                    putString(ProfileFragment.ARG_USER_ID, recipe.creatorId)
                }
            )
        }

        binding.commentButton.setOnCheckedChangeListener { _, _ ->
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            binding.bottomSheetViewPager.currentItem = binding.bottomSheetViewPager.adapter!!.itemCount - 1
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadMedia()

        viewModel.loadRecipe(recipeId)
        lifecycleScope.launch {
            viewModel.recipe.collect {
                if (it != null) {
                    setupBottomSheet(it)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.likes.collect { likes ->
                binding.like.text = likes.toString()
            }
        }

        lifecycleScope.launch {
            viewModel.isLiked.collect { liked ->
                binding.postLikeButton.isChecked = liked
                binding.bottomSheetLikeButton.isChecked = liked
            }
        }
        lifecycleScope.launch {
            viewModel.isSaved.collect { saved ->
                binding.bookmarkButton.isChecked = saved
                binding.bottomSheetSaveButton.isChecked = saved
            }
        }

        binding.postLikeButton.setOnClickListener { viewModel.toggleLike() }
        binding.bottomSheetLikeButton.setOnClickListener { viewModel.toggleLike() }

        binding.bookmarkButton.setOnClickListener { viewModel.toggleSave() }
        binding.bottomSheetSaveButton.setOnClickListener { viewModel.toggleSave() }
        binding.shoppingListButton.setOnClickListener {
            val dialogBinding = DialogAddToShoppingListBinding.inflate(LayoutInflater.from(this@FeedPostFragment.requireContext()))

            MaterialAlertDialogBuilder(this@FeedPostFragment.requireContext())
                .setTitle(getString(R.string.title_dialog_shoppingList))
                .setMessage(getString(R.string.message_dialog_shoppingList))
                .setView(dialogBinding.root)
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                .setPositiveButton(getString(R.string.add)) { _, _ ->
                    bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                    fun showErrorToast() {
                        Toast.makeText(requireContext(), getString(R.string.error_add_shoppingList), Toast.LENGTH_LONG).show()
                    }
                    lifecycleScope.launch {
                        val userResponse = RetrofitClient.apiService.getUser("me")
                        if (userResponse.isSuccessful) {
                            val newShoppingList = userResponse.body()?.shoppingList?.toMutableMap() ?: return@launch showErrorToast()
                            newShoppingList[recipeId] = dialogBinding.recipePortionsSelector.currentValue.value
                            val patchResponse = RetrofitClient.apiService.patchUser("me", PartialUser(shoppingList = newShoppingList))
                            if (patchResponse.isSuccessful) {
                                this@FeedPostFragment.findNavController().navigate(R.id.navigation_shopping_list)
                            } else {
                                showErrorToast()
                            }
                        } else {
                            showErrorToast()
                        }
                    }
                }
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        exoPlayer?.play()
    }


    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
        exoPlayer?.seekTo(0)
        binding.playButton.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exoPlayer?.release()
        exoPlayer = null
        _binding = null
    }
}
