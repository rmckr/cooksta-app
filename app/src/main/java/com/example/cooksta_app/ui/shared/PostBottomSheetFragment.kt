package com.example.cooksta_app.ui.shared

import android.app.Dialog
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.cooksta_app.R
import com.example.cooksta_app.ui.home.Post
import com.example.cooksta_app.ui.shoppingList.IngredientsListFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class PostBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var post: Post

    companion object {
        private const val POST_KEY = "post"

        fun newInstance(post: Post): PostBottomSheetFragment {
            return PostBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(POST_KEY, post)
                }
            }
        }
    }

    override fun onCreateDialog(
        savedInstanceState: Bundle?,
    ): Dialog {
        val bottomSheetDialog: BottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.modal_post_sheet)
        bottomSheetDialog.behavior.peekHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80f, resources.displayMetrics).toInt()
//        bottomSheetDialog.behavior.addBottomSheetCallback(BottomSheetTouchBehavior(viewPager, bottomSheetArrow))

        val bottomSheetArrow = bottomSheetDialog.findViewById<ImageView>(R.id.bottomSheetArrow)
        val bottomSheetTabLayout = bottomSheetDialog.findViewById<TabLayout>(R.id.bottomSheetTabLayout)
        val bottomSheetViewPager = bottomSheetDialog.findViewById<ViewPager2>(R.id.bottomSheetViewPager)
        val bottomSheetLikeButton = bottomSheetDialog.findViewById<MaterialButton>(R.id.bottomSheetLikeButton)
        val bottomSheetSaveButton = bottomSheetDialog.findViewById<MaterialButton>(R.id.bottomSheetSaveButton)

        post = requireArguments().getParcelable(POST_KEY, Post::class.java)!!

        class DescriptionFragment : Fragment() {
            protected lateinit var textView: TextView

            override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
            ): View {
                textView = TextView(requireContext())

                val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
                textView.setPadding(padding)
                textView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                textView.text = post.description
                return textView
            }
        }

        class FragmentAdapter(private val fragments: List<Fragment>) : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size

            override fun createFragment(position: Int): Fragment {
                return fragments[position]
            }
        }
        bottomSheetViewPager.adapter = FragmentAdapter(listOf(
            DescriptionFragment(),
            IngredientsListFragment(),
            CommentsFragment()
        ))
        TabLayoutMediator(bottomSheetTabLayout, bottomSheetViewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Beschreibung"
                1 -> "Zutaten"
                2 -> "Kommentare"
                else -> ""
            }
        }.attach()

        return bottomSheetDialog
    }

    class BottomSheetTouchBehavior(private val viewPager2: ViewPager2, private val arrowView: ImageView) : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(_bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_EXPANDED -> {
                    arrowView.animate().rotation(180f).setDuration(250)
                }
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    arrowView.animate().rotation(0f).setDuration(250)
                }
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
}