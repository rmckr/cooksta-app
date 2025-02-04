package com.example.cooksta_app.ui.home

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.FragmentHomeBinding
import com.example.cooksta_app.ui.shared.CommentsFragment
import com.example.cooksta_app.ui.shoppingList.IngredientsListFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.gpu.BrightnessFilterTransformation

data class Post(
    val imageUrl: String,
    val videoUrl: String?,
    val description: String = "1.Vorbereitung: Heize den Ofen auf 180 °C Ober-/Unterhitze vor und lege ein Backblech mit Backpapier aus. \n2. Teig anrühren: Gib die weiche Butter, den Zucker und den braunen Zucker in eine Schüssel. Schlage die Zutaten mit einem Handmixer oder einer Küchenmaschine schaumig. Füge das Ei und den Vanilleextrakt hinzu und rühre weiter, bis eine gleichmäßige Masse entsteht.\n3. Trockene Zutaten hinzufügen: Siebe das Mehl und das Backpulver in eine separate Schüssel. Vermische beides und gib es dann nach und nach zu der Butter-Zucker-Mischung. Rühre so lange, bis ein glatter Teig entsteht.\n4. Schokolade einrühren: Hacke die Zartbitterschokolade in kleine Stücke und hebe sie unter den Teig.\n5. Kekse formen: Mit einem Teelöffel nimm portionsweise Teig und forme kleine Kugeln. Lege sie mit ausreichend Abstand auf das vorbereitete Backblech. Die Kekse laufen beim Backen etwas auseinander, daher sollte der Abstand groß genug sein.\n6. Backen: Backe die Kekse für etwa 10-12 Minuten, bis sie goldbraun sind. Lass sie dann kurz abkühlen, bevor du sie vorsichtig vom Blech nimmst.\n7. Genießen: Die Schokoladenkekse sind noch warm besonders lecker. Lass sie vollständig auskühlen und bewahre sie in einer luftdichten Dose auf, wenn du sie später genießen möchtest.",
    val ingredients: Map<String, Float> = mapOf(Pair("Backpulver (TL)", 2f), Pair("Butter (g)", 200f), Pair("Eier (Stück)", 1f)),
    var isLiked: Boolean = false,
    var isSaved: Boolean = false
)

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val sampleData = listOf(
        Post("https://www.koch-mit.de/app/uploads/2020/01/jaegerpfanne-754x330.jpg", null),
        Post("https://example.com/image2.jpg", "https://files.vecteezy.com/system/resource/files/5273758/h81.mov?response-content-disposition=attachment%3Bfilename%3Dvecteezy_chef-cooking-with-fire-in-frying-pan-on-a-street-food-festival_5273758.mov&Expires=1737716047&Signature=p7q-XIC0C6zVU5HHpUOmuNMyspVPS-uL0dxECjf4GQwr~Sc3cNppjsfpfrEtmyajaOZz0ElqyjKqsyLKzzKGWyU78H3F6fOaEiMf-iFObIYyQIOlO5~ItV8GT4IZUvtlXekgUGPnnVWMNVh8dEck1k4XyxJXFjzspzQL5rgqlsKcH~Z2yIACX-mdQyv3Yy5fCBXH9T~68tCZYJvfX3w9IJJ3bcWxb6z9fpReWoZ5zfz7x~fEvlp9ez~1RDC91dEK9C~KU73lRGM60GsLpcwrlANCcJVA0yM~4c7T0EWr3CG15P7QAee1o~BIz8Y9v8YOA7pF~NCXFQuvV5fIMSC1Jw__&Key-Pair-Id=K3714PYOSHV3HB"),
        Post("https://www.simply-v.de/volumes/recipe/collections/_768x838_crop_center-center_none/rezepte-mittagessen-simply-v.jpg?v=1720165883", null)
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

        binding.homeViewPager.adapter = HomeFeedAdapter(this, binding.homeViewPager, sampleData)
        binding.homeViewPager.orientation = ViewPager2.ORIENTATION_VERTICAL

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class HomeFeedAdapter(
    private val fragment: Fragment,
    private val viewPager: ViewPager2,
    private val posts: List<Post>
): RecyclerView.Adapter<HomeFeedAdapter.HomePostViewHolder>() {

    inner class HomePostViewHolder(itemView: View) : ViewHolder(itemView) {
        private val postImage = itemView.findViewById<ImageView>(R.id.postImage)
        private val postImageBackdrop = itemView.findViewById<ImageView>(R.id.postImageBackdrop)
        private val postVideo = itemView.findViewById<VideoView>(R.id.postVideo)
        private val postLikeButton = itemView.findViewById<CheckBox>(R.id.postLikeButton)
        private val postCommentButton = itemView.findViewById<CheckBox>(R.id.commentButton)
        private val postSaveButton = itemView.findViewById<CheckBox>(R.id.bookmarkButton)
        private val bottomSheet = itemView.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        private val bottomSheetArrow = itemView.findViewById<ImageView>(R.id.bottomSheetArrow)
        private val bottomSheetTabLayout = itemView.findViewById<TabLayout>(R.id.bottomSheetTabLayout)
        private val bottomSheetViewPager = itemView.findViewById<ViewPager2>(R.id.bottomSheetViewPager)
        private val bottomSheetLikeButton = itemView.findViewById<MaterialButton>(R.id.bottomSheetLikeButton)
        private val bottomSheetSaveButton = itemView.findViewById<MaterialButton>(R.id.bottomSheetSaveButton)

        fun bind(post: Post) {
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
                    .transform(MultiTransformation(
                        BrightnessFilterTransformation(-0.25f),
                        BlurTransformation(25, 5)
                    ))
                    .into(postImageBackdrop)
                Glide.with(postImage).load(post.imageUrl).into(postImage)
            }

            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.addBottomSheetCallback(BottomSheetTouchBehavior(viewPager, bottomSheetArrow))
            bottomSheet.background.alpha = 0

            postLikeButton.setOnCheckedChangeListener { _, isChecked ->
                bottomSheetLikeButton.isChecked = isChecked
                // TODO: Make API call to like post
            }
            bottomSheetLikeButton.setOnClickListener { _ ->
                postLikeButton.isChecked = !postLikeButton.isChecked
            }
            postSaveButton.setOnCheckedChangeListener { _, isChecked ->
                bottomSheetSaveButton.isChecked = isChecked
                // TODO: Make API call to save post
            }
            bottomSheetSaveButton.setOnClickListener { _ ->
                postSaveButton.isChecked = !postSaveButton.isChecked
            }
            postCommentButton.setOnCheckedChangeListener { _, _ ->
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                bottomSheetViewPager.currentItem = bottomSheetViewPager.adapter!!.itemCount - 1
            }

            class FragmentAdapter(private val fragments: List<Fragment>) : FragmentStateAdapter(fragment) {
                override fun getItemCount() = fragments.size

                override fun createFragment(position: Int): Fragment {
                    return fragments[position]
                }
            }
            bottomSheetViewPager.adapter = FragmentAdapter(listOf(
                PostDescriptionFragment.newInstance(post.description),
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
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return HomePostViewHolder(view)
    }

    override fun getItemCount() = posts.size

    override fun onBindViewHolder(holder: HomePostViewHolder, position: Int) = holder.bind(posts[position])
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
