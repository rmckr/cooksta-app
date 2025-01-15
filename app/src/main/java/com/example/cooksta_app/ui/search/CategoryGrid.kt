package com.example.cooksta_app.ui.search

import android.graphics.Rect
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksta_app.R
import com.example.cooksta_app.ui.shared.RecyclerViewFragment


data class CategoryCard(
    val textResource: Int,
    val imageResource: Int
)

private val categories = listOf(
    CategoryCard(textResource = R.string.asianCategory, imageResource = R.drawable.ic_ramen),
    CategoryCard(textResource = R.string.bakeCategory, imageResource = R.drawable.ic_cake),
    CategoryCard(textResource = R.string.dessertCategory, imageResource = R.drawable.ic_icecream),
    CategoryCard(textResource = R.string.beverageCategory, imageResource = R.drawable.ic_liquor),
    CategoryCard(textResource = R.string.mainDishCategory, imageResource = R.drawable.ic_lunch),
    CategoryCard(textResource = R.string.italianCategory, imageResource = R.drawable.ic_pizza),
    CategoryCard(textResource = R.string.fastCategory, imageResource = R.drawable.ic_acute),
    CategoryCard(textResource = R.string.soupCategory, imageResource = R.drawable.ic_soup),
    CategoryCard(textResource = R.string.vegetarianCategory, imageResource = R.drawable.ic_apple),
)

class CategoryGrid : RecyclerViewFragment<CategoryAdapter>() {
    private val spanCount = 3
    private val spacing = 16

    override fun provideAdapter(): CategoryAdapter {
        return CategoryAdapter(categories)
    }

    override fun provideLayoutManager(): RecyclerView.LayoutManager {
        return GridLayoutManager(context, spanCount)
    }

    override fun provideDecoration(): RecyclerView.ItemDecoration {
        val pixelSpacing = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            spacing.toFloat(),
            resources.displayMetrics
        ).toInt()
        return object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                val column = position % spanCount
                outRect.left = if (column == 0) 0 else if (column < spanCount - 1) pixelSpacing / 4 else pixelSpacing / 2
                outRect.right = if (column == 0) pixelSpacing / 2 else if (column < spanCount - 1) pixelSpacing / 4 else 0
                outRect.top = if (position < spanCount) 0 else pixelSpacing
                outRect.bottom = 0
            }
        }
    }
}

class CategoryAdapter(
    private val cards: List<CategoryCard>
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    override fun getItemCount(): Int {
        return cards.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_card, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val card = cards[position]
        holder.bind(card)
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView = itemView.findViewById<ImageView>(R.id.imageCard)
        private val textView = itemView.findViewById<TextView>(R.id.textViewCard)

        fun bind(card: CategoryCard) {
            imageView.setImageResource(card.imageResource)
            textView.setText(card.textResource)
        }
    }
}