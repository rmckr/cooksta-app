package com.example.cooksta_app.ui.search

import android.graphics.Rect
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.ItemCategoryCardBinding
import com.example.cooksta_app.ui.shared.RecyclerViewFragment


data class CategoryCard(
    val target: String,
    val textResource: Int,
    val imageResource: Int
)

// TODO: Get targets from api
private val categories = listOf(
    CategoryCard(target = "asian", textResource = R.string.asianCategory, imageResource = R.drawable.card_ramen),
    CategoryCard(target = "baking", textResource = R.string.bakeCategory, imageResource = R.drawable.card_baking),
    CategoryCard(target = "dessert", textResource = R.string.dessertCategory, imageResource = R.drawable.card_dessert),
    CategoryCard(target = "drink", textResource = R.string.beverageCategory, imageResource = R.drawable.card_drinks),
    CategoryCard(target = "main", textResource = R.string.mainDishCategory, imageResource = R.drawable.card_lunch),
    CategoryCard(target = "italian", textResource = R.string.italianCategory, imageResource = R.drawable.card_pizza),
    CategoryCard(target = "quick", textResource = R.string.fastCategory, imageResource = R.drawable.card_fast),
    CategoryCard(target = "soup", textResource = R.string.soupCategory, imageResource = R.drawable.card_soup),
    CategoryCard(target = "vegetarian", textResource = R.string.vegetarianCategory, imageResource = R.drawable.card_veggie),
)

class CategoryGridFragment : RecyclerViewFragment<CategoryGridFragment.Adapter>(), CategoryInteractionListener {
    private val spanCount = 3
    private val spacing = 16

    override fun onCardClick(target: CategoryCard) {
        val searchFragment = requireParentFragment() as SearchFragment
        searchFragment.handleCategoryClick(target)
    }

    override fun provideAdapter(): Adapter {
        return Adapter(categories, this)
    }

    override fun provideLayoutManager(): RecyclerView.LayoutManager {
        return GridLayoutManager(context, spanCount)
    }

    override fun provideDecoration(): RecyclerView.ItemDecoration {
        return object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val pixelSpacing = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    spacing.toFloat(),
                    resources.displayMetrics
                ).toInt()
                val position = parent.getChildAdapterPosition(view)
                val column = position % spanCount
                outRect.left = column * pixelSpacing / spanCount
                outRect.right = pixelSpacing - (column + 1) * pixelSpacing / spanCount
                outRect.top = if (position < spanCount) 0 else pixelSpacing
                outRect.bottom = 0

            }
        }
    }

    inner class Adapter(private val cards: List<CategoryCard>, private val interactionListener: CategoryInteractionListener) : RecyclerView.Adapter<Adapter.CategoryCardViewHolder>() {
        override fun getItemCount() = cards.size

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CategoryCardViewHolder {
            val binding = ItemCategoryCardBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            return CategoryCardViewHolder(binding)
        }

        override fun onBindViewHolder(holder: CategoryCardViewHolder, position: Int) {
            val card = cards[position]
            holder.bind(card)
        }

        inner class CategoryCardViewHolder(private val binding: ItemCategoryCardBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(card: CategoryCard) {
                binding.card.setOnClickListener {
                    interactionListener.onCardClick(card)
                }
                binding.imageCard.setImageResource(card.imageResource)
                binding.textViewCard.setText(card.textResource)
            }
        }
    }
}

interface CategoryInteractionListener {
    fun onCardClick(target: CategoryCard)
}
