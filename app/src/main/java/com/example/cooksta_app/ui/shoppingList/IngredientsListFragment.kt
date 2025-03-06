package com.example.cooksta_app.ui.shoppingList

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.ItemShoppingListIngredientBinding
import com.example.cooksta_app.model.Ingredient
import com.example.cooksta_app.ui.shared.RecyclerViewFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class IngredientsListFragment : RecyclerViewFragment<IngredientsListFragment.IngredientsAdapter>() {

    private val ingredients = MutableStateFlow<List<Ingredient>>(emptyList())

    private val hideCheckboxes: Boolean by lazy {
        arguments?.getBoolean(ARG_HIDE_CHECKBOXES) ?: false
    }

    companion object {
        private const val ARG_HIDE_CHECKBOXES = "hideCheckboxes"

        fun withoutCheckboxes(): IngredientsListFragment {
            val fragment = IngredientsListFragment()
            val args = Bundle()
            args.putBoolean(ARG_HIDE_CHECKBOXES, true)
            fragment.arguments = args
            return fragment
        }
    }

    override fun provideAdapter(): IngredientsAdapter {
        return IngredientsAdapter()
    }

    override fun onRecyclerViewCreated(recyclerView: RecyclerView) {
        lifecycleScope.launch {
            ingredients.collect {
                adapter.submitList(it)
            }
        }
    }

    fun submitIngredients(ingredients: List<Ingredient>) {
        this.ingredients.value = ingredients
    }

    class DiffCallback : DiffUtil.ItemCallback<Ingredient>() {
        override fun areItemsTheSame(oldItem: Ingredient, newItem: Ingredient): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Ingredient, newItem: Ingredient): Boolean {
            return oldItem == newItem
        }
    }

    inner class IngredientsAdapter : ListAdapter<Ingredient, IngredientsAdapter.IngredientViewHolder>(DiffCallback()) {

        inner class IngredientViewHolder(private val binding: ItemShoppingListIngredientBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(ingredient: Ingredient) {
                val defaultColor = ContextCompat.getColor(binding.ingredientName.context, R.color.md_theme_onSurface)
                val checkedColor = ContextCompat.getColor(binding.ingredientName.context, R.color.md_theme_primary)

                binding.ingredientName.text = ingredient.name
                binding.ingredientName.paintFlags = if (ingredient.isChecked) Paint.STRIKE_THRU_TEXT_FLAG else 0
                binding.ingredientName.setTextColor(if (ingredient.isChecked) checkedColor else defaultColor)

                if (ingredient.amount == null) {
                    binding.ingredientAmount.visibility = View.GONE
                } else {
                    binding.ingredientAmount.visibility = View.VISIBLE
                    binding.ingredientAmount.text = ingredient.amount.toString()
                }

                if (hideCheckboxes) {
                    binding.checkBox.visibility = View.GONE
                } else {
                    binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                        ingredient.isChecked = isChecked
                        binding.ingredientName.paintFlags = if (isChecked) Paint.STRIKE_THRU_TEXT_FLAG else 0
                        binding.ingredientName.setTextColor(if (isChecked) checkedColor else defaultColor)
                    }
                    binding.ingredientCard.setOnClickListener { binding.checkBox.isChecked = !binding.checkBox.isChecked }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
            val binding = ItemShoppingListIngredientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return IngredientViewHolder(binding)
        }

        override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
            val ingredient = getItem(position)
            holder.bind(ingredient)
        }
    }
}
