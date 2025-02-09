package com.example.cooksta_app.ui.shared

import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.example.cooksta_app.databinding.ItemPostBinding
import com.example.cooksta_app.model.Post
import com.example.cooksta_app.ui.home.PostDescriptionFragment
import com.example.cooksta_app.ui.shoppingList.IngredientsListFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayoutMediator
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.gpu.BrightnessFilterTransformation

class FeedPostViewHolder(
    private val binding: ItemPostBinding,
    private val fragment: FeedFragment,
    private val feedViewPager: ViewPager2
) : ViewHolder(binding.root) {
    fun bind(post: Post) {
        with(binding) {
            if (post.videoUrl != null) {
                postVideo.visibility = View.VISIBLE
                val videoUri = Uri.parse(post.videoUrl)
                postVideo.setVideoURI(videoUri)
                postVideo.setOnClickListener {
                    postVideo.start()
                }
            } else {
                postVideo.visibility = View.GONE

                Glide.with(postImageBackdrop).load(post.imageUrl)
                    .transform(
                        MultiTransformation(
                            BrightnessFilterTransformation(-0.25f),
                            BlurTransformation(25, 5)
                        )
                    )
                    .into(postImageBackdrop)
                Glide.with(postImage).load(post.imageUrl).into(postImage)
            }

            class BottomSheetTouchBehavior(private val viewPager2: ViewPager2, private val arrowView: ImageView) : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        arrowView.animate().rotation(180f).setDuration(250)
                    } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        arrowView.animate().rotation(0f).setDuration(250)
                    }
                    viewPager2.isUserInputEnabled = when (newState) {
                        BottomSheetBehavior.STATE_DRAGGING, BottomSheetBehavior.STATE_EXPANDED -> { false }
                        else -> { true }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    bottomSheet.background.alpha = (slideOffset * 255).toInt().coerceIn(0, 255)
                }
            }
            val bottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)
            bottomSheetBehavior.addBottomSheetCallback(
                BottomSheetTouchBehavior(feedViewPager, bottomSheetArrow)
            )
            standardBottomSheet.background.alpha = 0

            postLikeButton.setOnCheckedChangeListener { _, isChecked ->
                bottomSheetLikeButton.isChecked = isChecked
                // TODO: Make API call to like post
            }
            bottomSheetLikeButton.setOnClickListener { _ ->
                postLikeButton.isChecked = !postLikeButton.isChecked
            }
            bookmarkButton.setOnCheckedChangeListener { _, isChecked ->
                bottomSheetSaveButton.isChecked = isChecked
                // TODO: Make API call to save post
            }
            bottomSheetSaveButton.setOnClickListener { _ ->
                bookmarkButton.isChecked = !bookmarkButton.isChecked
            }
            commentButton.setOnCheckedChangeListener { _, _ ->
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                bottomSheetViewPager.currentItem = bottomSheetViewPager.adapter!!.itemCount - 1
            }

            class FragmentAdapter(private val fragments: List<Fragment>) : FragmentStateAdapter(fragment) {
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
        }
    }
}
