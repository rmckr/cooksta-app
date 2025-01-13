package com.example.cooksta_app.ui.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val categoryNames = listOf(
        R.string.asianCategory,
        R.string.bakeCategory,
        R.string.dessertCategory,
        R.string.beverageCategory,
        R.string.mainDishCategory,
        R.string.italianCategory,
        R.string.fastCategory,
        R.string.soupCategory,
        R.string.vegetarianCategory
    )
    private val categoryImages = listOf(
        R.drawable.ic_ramen,
        R.drawable.ic_cake,
        R.drawable.ic_icecream,
        R.drawable.ic_liquor,
        R.drawable.ic_lunch,
        R.drawable.ic_pizza,
        R.drawable.ic_acute,
        R.drawable.ic_soup,
        R.drawable.ic_apple
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        val gridView = binding.root.findViewById<GridView>(R.id.gridViewCard)
        val adapter = CategoryAdapter(requireContext(), categoryNames, categoryImages)
        gridView.adapter = adapter

        gridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val category = categoryNames[position]
            Toast.makeText(requireContext(), "Kategorie: $category", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class CategoryAdapter(
    private val context: Context,
    private val categoryNames: List<Int>,
    private val categoryImages: List<Int>
) : android.widget.BaseAdapter() {

    override fun getCount(): Int = categoryNames.size

    override fun getItem(position: Int): Any = categoryNames[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_category_card, parent, false)

        val imageView = view.findViewById<ImageView>(R.id.imageCard)
        val textView = view.findViewById<TextView>(R.id.textViewCard)

        imageView.setImageResource(categoryImages[position])
        textView.setText(categoryNames[position])
        return view
    }
}
