package com.example.cooksta_app.ui.shared

import android.animation.ArgbEvaluator
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.FragmentPostBinding
import com.example.cooksta_app.model.Ingredient
import com.example.cooksta_app.network.RetrofitClient
import com.example.cooksta_app.ui.home.PostDescriptionFragment
import com.example.cooksta_app.ui.shoppingList.IngredientsListFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.gpu.BrightnessFilterTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private val recipeId: String by lazy {
        arguments?.getString(ARG_RECIPE_ID) ?: throw IllegalStateException("$ARG_RECIPE_ID argument is missing")
    }

    private var isLiked = MutableStateFlow(false)
    private var isSaved = MutableStateFlow(false)

    private var cachedFile: File? = null
    private var exoPlayer: ExoPlayer? = null

    private suspend fun getMediaFromCacheOrStream(): File? {
        val cacheDir = requireContext().cacheDir

        for (file in cacheDir.listFiles()!!) {
            if (file.name.contains(recipeId) && file.length() > 0) {
                return file
            }
        }

        val mediaResponse = RetrofitClient.apiService.getRecipeMedia(recipeId)
        if (!mediaResponse.isSuccessful) { return null }

        val contentType = mediaResponse.headers()["Content-Type"] ?: ""
        val extension = when {
            contentType.contains("image/png") -> ".png"
            contentType.contains("video/mp4") -> ".mp4"
            else -> null
        }

        val mediaStream = mediaResponse.body()?.byteStream()

        if (mediaStream !== null && extension !== null) {
            return withContext(Dispatchers.IO) {
                val tempFile = File.createTempFile("media-$recipeId", extension, cacheDir).apply { deleteOnExit() }
                tempFile.outputStream().use { output ->
                    mediaStream.use { input -> input.copyTo(output) }
                }
                tempFile
            }
        }

        return null
    }

    private fun loadMedia() {
        lifecycleScope.launch {
            val tempFile = getMediaFromCacheOrStream()

            if (tempFile !== null) {
                when (tempFile.extension) {
                    "png" -> {
                        with(binding) {
                            postVideoTexture.visibility = View.GONE
                            postImage.visibility = View.VISIBLE
                            postImageBackdrop.visibility = View.VISIBLE

                            Glide.with(this@FeedPostFragment)
                                .load(tempFile)
                                .transform(
                                    MultiTransformation(
                                        BrightnessFilterTransformation(-0.25f),
                                        BlurTransformation(25, 5)
                                    )
                                )
                                .into(postImageBackdrop)

                            Glide.with(this@FeedPostFragment)
                                .load(tempFile)
                                .into(postImage)
                        }
                    }
                    "mp4" -> {
                        with(binding) {
                            postVideoTexture.visibility = View.VISIBLE
                            postImage.visibility = View.GONE
                            postImageBackdrop.visibility = View.GONE

                            val exoPlayer = ExoPlayer.Builder(binding.root.context).build()
                            binding.postVideoTexture.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                                override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                                    exoPlayer.setVideoTextureView(binding.postVideoTexture)
                                    exoPlayer.setMediaItem(MediaItem.fromUri(tempFile.toUri()))
                                    exoPlayer.prepare()
                                    exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
                                }

                                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
                                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                                    exoPlayer.setVideoTextureView(null)
                                    return true
                                }

                                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                            }

                            this@FeedPostFragment.exoPlayer = exoPlayer
                        }
                    }
                    else -> Toast.makeText(requireContext(), getString(R.string.toast_unsupported_media), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.toast_unavailable_media), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBottomSheet() {
        lifecycleScope.launch {
            val recipeResponse = RetrofitClient.apiService.getRecipe(recipeId)
            val recipe = recipeResponse.body()
            if (recipeResponse.isSuccessful && recipe !== null) {
                with(binding) {
                    bottomSheetTitle.load(recipe.title)

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
                            bottomSheetLayout.setBackgroundColor(interpolatedBackground)
                            bottomSheetLayout.background.alpha =
                                (slideOffset.coerceIn(minBottomSheetAlpha, 1f) * 255).toInt()
                            val interpolatedColor = evaluator.evaluate(
                                slideOffset.coerceIn(0f, 1f),
                                Color.WHITE,
                                ContextCompat.getColor(requireContext(), R.color.md_theme_onSurface)
                            ) as Int
                            bottomSheetTitle.setTextColor(interpolatedColor)
                            bottomSheetArrow.imageTintList = ColorStateList.valueOf(interpolatedColor)
                            bottomSheetArrow.rotation = slideOffset * 180f
                        }
                    }
                    bottomSheetLayout.setBackgroundColor(Color.BLACK)
                    bottomSheetLayout.background.alpha = (minBottomSheetAlpha * 255).toInt()
                    val bottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)
                    bottomSheetBehavior.addBottomSheetCallback(BottomSheetTouchBehavior())

                    class FragmentAdapter(private val fragments: List<Fragment>) :
                        FragmentStateAdapter(this@FeedPostFragment) {
                        override fun getItemCount() = fragments.size

                        override fun createFragment(position: Int): Fragment {
                            return fragments[position]
                        }
                    }

                    val ingredientsListFragment = IngredientsListFragment.withoutCheckboxes()
                    ingredientsListFragment.submitIngredients(recipe.ingredients.map { Ingredient(it.key, it.value) })
                    bottomSheetViewPager.adapter = FragmentAdapter(
                        listOf(
                            PostDescriptionFragment.newInstance(recipe.description),
                            ingredientsListFragment,
                            CommentsFragment()
                        )
                    )
                    TabLayoutMediator(bottomSheetTabLayout, bottomSheetViewPager) { tab, position ->
                        tab.text = when (position) {
                            0 -> getString(R.string.description)
                            1 -> getString(R.string.ingredients)
                            2 -> getString(R.string.comments)
                            else -> ""
                        }
                    }.attach()

                    commentButton.setOnCheckedChangeListener { _, _ ->
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        bottomSheetViewPager.currentItem = bottomSheetViewPager.adapter!!.itemCount - 1
                    }
                }
            }
        }
    }

    private fun toggleLike() {
        val optimisticNewState = !isLiked.value
        isLiked.value = optimisticNewState

        // TODO: API endpoints not implemented
/*        viewHolderScope.launch {
            val response = if (!isLiked.value) {
                RetrofitClient.apiService.postRecipeLike(postId)
            } else {
                RetrofitClient.apiService.deleteRecipeLike(postId)
            }
            isLiked.value = if (response.isSuccessful) {
                response.body()?.state ?: !optimisticNewState
            } else {
                !optimisticNewState
            }
        }*/
    }

    private fun toggleSave() {
        val optimisticNewState = !isSaved.value
        isSaved.value = optimisticNewState

        // TODO: API endpoints not implemented
/*        viewHolderScope.launch {
            val response = if (!isLiked.value) {
                RetrofitClient.apiService.postRecipeSave(postId)
            } else {
                RetrofitClient.apiService.deleteRecipeSave(postId)
            }
            isLiked.value = if (response.isSuccessful) {
                response.body()?.state ?: !optimisticNewState
            } else {
                !optimisticNewState
            }
        }*/
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPostBinding.inflate(inflater, container, false)

        with(binding) {
            loadMedia()
            if (exoPlayer !== null) {
                binding.postVideoTexture.setOnClickListener {
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

            setupBottomSheet()

            lifecycleScope.launch {
                isLiked.collect { liked ->
                    postLikeButton.isChecked = liked
                    bottomSheetLikeButton.isChecked = liked
                }
            }

            lifecycleScope.launch {
                isSaved.collect { saved ->
                    bookmarkButton.isChecked = saved
                    bottomSheetSaveButton.isChecked = saved
                }
            }

            postLikeButton.setOnClickListener { toggleLike() }
            bottomSheetLikeButton.setOnClickListener { toggleLike() }

            bookmarkButton.setOnClickListener { toggleSave() }
            bottomSheetSaveButton.setOnClickListener { toggleSave() }
            shoppingListButton.setOnClickListener {
                val dialogView = LayoutInflater.from(this@FeedPostFragment.requireContext())
                    .inflate(R.layout.dialog_add_to_shopping_list, null)

                MaterialAlertDialogBuilder(this@FeedPostFragment.requireContext())
                    .setTitle(getString(R.string.title_dialog_shoppingList))
                    .setMessage(getString(R.string.message_dialog_shoppingList))
                    .setView(dialogView)
                    .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton(getString(R.string.add)) { _, _ ->
                        this@FeedPostFragment.findNavController().navigate(R.id.navigation_shopping_list)
                    }
                    .show()
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
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
        cachedFile?.delete()
        exoPlayer?.release()
        exoPlayer = null
        _binding = null
    }
}
