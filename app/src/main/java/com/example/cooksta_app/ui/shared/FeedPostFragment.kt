package com.example.cooksta_app.ui.shared

import android.animation.ArgbEvaluator
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
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
import com.example.cooksta_app.databinding.ItemPostBinding
import com.example.cooksta_app.model.Recipe
import com.example.cooksta_app.network.RetrofitClient
import com.example.cooksta_app.ui.home.PostDescriptionFragment
import com.example.cooksta_app.ui.shoppingList.IngredientsListFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.gpu.BrightnessFilterTransformation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class FeedPostFragment : Fragment() {

    companion object {
        private const val ARG_RECIPE = "recipe"

        fun newInstance(recipe: Recipe): FeedPostFragment {
            val fragment = FeedPostFragment()
            val bundle = Bundle()
            bundle.putParcelable(ARG_RECIPE, recipe)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var _binding: ItemPostBinding? = null
    private val binding get() = _binding!!

    private var exoPlayer: ExoPlayer? = null

    private val post: Recipe by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_RECIPE, Recipe::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(ARG_RECIPE)
        } ?: throw IllegalStateException("Recipe argument is missing")
    }

    private var isLiked = MutableStateFlow(false)
    private var isSaved = MutableStateFlow(false)

    private fun loadMedia(recipeId: String) {
        lifecycleScope.launch {
            val mediaResponse = RetrofitClient.apiService.getRecipeMedia(recipeId)
            val mediaStream = mediaResponse.body()?.byteStream()
            if (mediaResponse.isSuccessful && mediaStream !== null) {
                val contentType = mediaResponse.headers()["Content-Type"] ?: ""
                when {
                    contentType.startsWith("image") -> with(binding) {
                        postVideoTexture.visibility = View.GONE
                        postImage.visibility = View.VISIBLE
                        postImageBackdrop.visibility = View.VISIBLE

                        Glide.with(this@FeedPostFragment).load(mediaStream)
                            .transform(
                                MultiTransformation(
                                    BrightnessFilterTransformation(-0.25f),
                                    BlurTransformation(25, 5)
                                )
                            )
                            .into(postImageBackdrop)
                        Glide.with(this@FeedPostFragment).load(mediaStream).into(postImage)
                    }
                    contentType.startsWith("video") -> with(binding) {
                        postVideoTexture.visibility = View.VISIBLE
                        postImage.visibility = View.GONE
                        postImageBackdrop.visibility = View.GONE

//                        val tempFile = withContext(Dispatchers.IO) {
//                            val tempFile = File.createTempFile("video-$recipeId", ".mp4", fragment.requireContext().cacheDir).apply { deleteOnExit() }
//                            mediaStream.use { input ->
//                                FileOutputStream(tempFile).use { output ->
//                                    input.copyTo(output)
//                                }
//                            }
//                            return@withContext tempFile
//                        }

                        val exoPlayer = ExoPlayer.Builder(binding.root.context).build()
                        binding.postVideoTexture.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                            override fun onSurfaceTextureAvailable(
                                surface: SurfaceTexture,
                                width: Int,
                                height: Int
                            ) {
                                exoPlayer.setVideoTextureView(binding.postVideoTexture)
                                exoPlayer.setMediaItem(MediaItem.fromUri(mediaResponse.raw().request.url.toString()))
                                exoPlayer.prepare()
                                exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
                                exoPlayer.playWhenReady = true
                            }

                            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
                            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                                exoPlayer.setVideoTextureView(null)
                                return true
                            }

                            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                        }
                        binding.postVideoTexture.setOnClickListener {
                            exoPlayer.playWhenReady = !exoPlayer.isPlaying
                        }
                        this@FeedPostFragment.exoPlayer = exoPlayer
                    }
                    else -> Toast.makeText(this@FeedPostFragment.requireContext(), "Unsupported media type", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@FeedPostFragment.requireContext(), "Media is unavailable", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBottomSheet(post: Recipe) {
        val evaluator = ArgbEvaluator()
        val startColor = Color.WHITE
        val endColor = ContextCompat.getColor(requireContext(), R.color.md_theme_onSurface)

        with(binding) {
            bottomSheetTitle.text = post.title

            class BottomSheetTouchBehavior : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        bottomSheetArrow.animate().rotation(180f).setDuration(250)
                    } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        bottomSheetArrow.animate().rotation(0f).setDuration(250)
                    }
                    when (newState) {
                        BottomSheetBehavior.STATE_DRAGGING, BottomSheetBehavior.STATE_EXPANDED -> { (requireParentFragment() as FeedFragment).setViewPagerInput(false) }
                        else -> { (requireParentFragment() as FeedFragment).setViewPagerInput(true) }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    bottomSheet.background.alpha = (slideOffset * 255).toInt().coerceIn(0, 255)
                    val interpolatedColor = evaluator.evaluate(slideOffset, startColor, endColor) as Int
                    bottomSheetTitle.setTextColor(interpolatedColor)
                }
            }
            val bottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)
            bottomSheetBehavior.addBottomSheetCallback(BottomSheetTouchBehavior())
            standardBottomSheet.background.alpha = 0

            class FragmentAdapter(private val fragments: List<Fragment>) : FragmentStateAdapter(this@FeedPostFragment) {
                override fun getItemCount() = fragments.size

                override fun createFragment(position: Int): Fragment {
                    return fragments[position]
                }
            }
            bottomSheetViewPager.adapter = FragmentAdapter(
                listOf(
                    PostDescriptionFragment.newInstance(post.description),
                    IngredientsListFragment(),
                    CommentsFragment()
                )
            )
            TabLayoutMediator(bottomSheetTabLayout, bottomSheetViewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> "Beschreibung"
                    1 -> "Zutaten"
                    2 -> "Kommentare"
                    else -> ""
                }
            }.attach()

            commentButton.setOnCheckedChangeListener { _, _ ->
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                bottomSheetViewPager.currentItem = bottomSheetViewPager.adapter!!.itemCount - 1
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
        _binding = ItemPostBinding.inflate(inflater, container, false)

        with(binding) {
            Log.d("TEST", "bind: $post")
            loadMedia(post.id)

            setupBottomSheet(post)

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
                    .setTitle("Rezept hinzufügen?")
                    .setMessage("Gib' die Portionen an, für die du das Rezept einkaufen willst:")
                    .setView(dialogView)
                    .setNegativeButton("Abbrechen") { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton("Hinzufügen") { _, _ ->
                        this@FeedPostFragment.findNavController().navigate(R.id.navigation_shopping_list)
                    }
                    .show()
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        exoPlayer?.playWhenReady = true
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exoPlayer?.release()
        exoPlayer = null
        _binding = null
    }
}
