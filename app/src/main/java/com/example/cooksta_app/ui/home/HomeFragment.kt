package com.example.cooksta_app.ui.home

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.VideoView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.FragmentHomeBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.sidesheet.SideSheetBehavior

data class FeedPost(
    val imageUrl: String,
    val videoUrl: String?
)

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val sampleData = listOf(
        FeedPost("https://www.koch-mit.de/app/uploads/2020/01/jaegerpfanne-754x330.jpg", null),
        FeedPost("https://example.com/image2.jpg", "https://scontent.cdninstagram.com/o1/v/t16/f2/m86/AQMUiIXYHFd5VcbMRxgsPoNq9T1hGliT4qWNnfdLCvhyxir_Ua4tEGLQk_IVTOWPmkxh4iKTfFd0J9BXr5DpUhD-MbQbosKFPcICcuA.mp4?efg=eyJ4cHZfYXNzZXRfaWQiOjM4MTc0OTA3MDE4NzIxMTMsInZlbmNvZGVfdGFnIjoieHB2X3Byb2dyZXNzaXZlLklOU1RBR1JBTS5DTElQUy5DMy4zNjAuZGFzaF9iYXNlbGluZV8zX3YxIn0&_nc_ht=scontent.cdninstagram.com&_nc_cat=100&vs=49c55c77dddab1dc&_nc_vs=HBksFQIYUmlnX3hwdl9yZWVsc19wZXJtYW5lbnRfc3JfcHJvZC85RDQyNDNDNjE1NkU2OUY1ODlDODU0QUEyNDhENEM4OF92aWRlb19kYXNoaW5pdC5tcDQVAALIAQAVAhg6cGFzc3Rocm91Z2hfZXZlcnN0b3JlL0dGX2k5eHVOQmNQNWpab0VBSjgteTY1MVVqdEpicV9FQUFBRhUCAsgBACgAGAAbAogHdXNlX29pbAExEnByb2dyZXNzaXZlX3JlY2lwZQExFQAAJuKvv5Oa_8cNFQIoAkMzLBdANfdLxqfvnhgSZGFzaF9iYXNlbGluZV8zX3YxEQB1_gcA&ccb=9-4&oh=00_AYBbnbqnwlI26PmR_xL4aBSelMZFp89oAcOQMhGzo5r0pQ&oe=67912E05&_nc_sid=1d576d"),
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

        binding.homeViewPager.adapter = HomeFeedAdapter(sampleData)
        binding.homeViewPager.orientation = ViewPager2.ORIENTATION_VERTICAL

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }
}

class HomeFeedAdapter(
    private val posts: List<FeedPost>
): RecyclerView.Adapter<HomeFeedAdapter.HomePostViewHolder>() {

    inner class HomePostViewHolder(itemView: View) : ViewHolder(itemView) {
        private val imageView = itemView.findViewById<ImageView>(R.id.imageViewHome)
        private val videoView = itemView.findViewById<VideoView>(R.id.videoView)
        private val sideSheet = itemView.findViewById<LinearLayout>(R.id.sideSheet)
        private val sideSheetButton = itemView.findViewById<MaterialButton>(R.id.sideSheetButton)
        private val sideSheetButtonBack = itemView.findViewById<MaterialButton>(R.id.sideSheetButtonBack)
        private val heartButton = itemView.findViewById<CheckBox>(R.id.heartButton)
        private val commentButton = itemView.findViewById<CheckBox>(R.id.commentButton)
        private val bookmarkButton = itemView.findViewById<CheckBox>(R.id.bookmarkButton)


        fun bind(post: FeedPost) {
            if (post.videoUrl != null) {
                videoView.visibility = View.VISIBLE
                val videoUri = Uri.parse(post.videoUrl)
                videoView.setVideoURI(videoUri)
                videoView.start()
            } else {
                videoView.visibility = View.GONE
            }
            Glide.with(imageView).load(post.imageUrl).into(imageView)

            heartButton.setOnCheckedChangeListener { checkBox, state ->
                // TODO: Handle button checked
            }
            commentButton.setOnCheckedChangeListener { checkBox, state ->
                // TODO: Handle button checked
            }
            bookmarkButton.setOnCheckedChangeListener { checkBox, state ->
                // TODO: Handle button checked
            }

            val standardSideSheetBehavior = SideSheetBehavior.from(sideSheet)
            sideSheetButton.setOnClickListener {
                standardSideSheetBehavior.expand()
            }
            sideSheetButtonBack.setOnClickListener {
                standardSideSheetBehavior.hide()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_post, parent, false)
        return HomePostViewHolder(view)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: HomePostViewHolder, position: Int) {
        holder.bind(posts[position])
    }
}