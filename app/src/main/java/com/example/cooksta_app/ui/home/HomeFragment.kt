package com.example.cooksta_app.ui.home

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.VideoView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.gpu.BrightnessFilterTransformation

data class FeedPost(
    val imageUrl: String,
    val videoUrl: String?
)

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val sampleData = listOf(
        FeedPost("https://www.koch-mit.de/app/uploads/2020/01/jaegerpfanne-754x330.jpg", null),
        FeedPost("https://example.com/image2.jpg", "https://files.vecteezy.com/system/resource/files/5273758/h81.mov?response-content-disposition=attachment%3Bfilename%3Dvecteezy_chef-cooking-with-fire-in-frying-pan-on-a-street-food-festival_5273758.mov&Expires=1737716047&Signature=p7q-XIC0C6zVU5HHpUOmuNMyspVPS-uL0dxECjf4GQwr~Sc3cNppjsfpfrEtmyajaOZz0ElqyjKqsyLKzzKGWyU78H3F6fOaEiMf-iFObIYyQIOlO5~ItV8GT4IZUvtlXekgUGPnnVWMNVh8dEck1k4XyxJXFjzspzQL5rgqlsKcH~Z2yIACX-mdQyv3Yy5fCBXH9T~68tCZYJvfX3w9IJJ3bcWxb6z9fpReWoZ5zfz7x~fEvlp9ez~1RDC91dEK9C~KU73lRGM60GsLpcwrlANCcJVA0yM~4c7T0EWr3CG15P7QAee1o~BIz8Y9v8YOA7pF~NCXFQuvV5fIMSC1Jw__&Key-Pair-Id=K3714PYOSHV3HB"),
        FeedPost("https://www.simply-v.de/volumes/recipe/collections/_768x838_crop_center-center_none/rezepte-mittagessen-simply-v.jpg?v=1720165883", null)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }

            WindowInsetsCompat.CONSUMED
        }

        binding.homeViewPager.adapter = HomeFeedAdapter(binding.homeViewPager, sampleData)
        binding.homeViewPager.orientation = ViewPager2.ORIENTATION_VERTICAL

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class HomeFeedAdapter(
    private val viewPager: ViewPager2,
    private val posts: List<FeedPost>
): RecyclerView.Adapter<HomeFeedAdapter.HomePostViewHolder>() {

    inner class HomePostViewHolder(itemView: View) : ViewHolder(itemView) {
        private val imageView = itemView.findViewById<ImageView>(R.id.imageViewHome)
        private val imageViewBackdrop = itemView.findViewById<ImageView>(R.id.imageViewHomeBackdrop)
        private val videoView = itemView.findViewById<VideoView>(R.id.videoView)
        private val heartButton = itemView.findViewById<CheckBox>(R.id.heartButton)
        private val commentButton = itemView.findViewById<CheckBox>(R.id.commentButton)
        private val bookmarkButton = itemView.findViewById<CheckBox>(R.id.bookmarkButton)
        private val bottomSheet = itemView.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        private val bottomSheetArrow = itemView.findViewById<ImageView>(R.id.bottomSheetArrow)

        fun bind(post: FeedPost) {
            if (post.videoUrl != null) {
                videoView.visibility = View.VISIBLE
                val videoUri = Uri.parse(post.videoUrl)
                videoView.setVideoURI(videoUri)
                videoView.setOnClickListener {
                    videoView.start()
                }
            } else {
                videoView.visibility = View.GONE
            }

            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.addBottomSheetCallback(BottomSheetTouchBehavior(viewPager, bottomSheetArrow))
            bottomSheet.background.alpha = 0

//            coordinatorLayout.post {
//                postAppBarLayout.post {
//                    Log.d("TEST", "bind: ${coordinatorLayout.measuredHeight} ${postAppBarLayout.measuredHeight}")
//                    bottomSheetBehavior.maxHeight = coordinatorLayout.height - postAppBarLayout.height
//                }
//            }

            Glide.with(imageViewBackdrop).load(post.imageUrl)
                .transform(MultiTransformation(
                    BrightnessFilterTransformation(-0.25f),
                    BlurTransformation(25, 5)
                ))
                .into(imageViewBackdrop)
            Glide.with(imageView).load(post.imageUrl).into(imageView)

            heartButton.setOnCheckedChangeListener { button, isChecked ->
                // TODO: Handle button checked
            }
            commentButton.setOnCheckedChangeListener { button, isChecked ->
                // TODO: Handle button checked
            }
            bookmarkButton.setOnCheckedChangeListener { button, isChecked ->
                // TODO: Handle button checked
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return HomePostViewHolder(view)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: HomePostViewHolder, position: Int) {
        holder.bind(posts[position])
    }
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
