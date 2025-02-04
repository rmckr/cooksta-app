package com.example.cooksta_app.ui.shoppingList

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.FragmentShoppingListBinding
import com.example.cooksta_app.ui.home.PostDescriptionFragment
import com.example.cooksta_app.ui.shared.RecyclerViewFragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textview.MaterialTextView
import kotlin.math.abs

class ShoppingListFragment : Fragment() {

    private var _binding: FragmentShoppingListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ShoppingListViewModel
    private var deleteDialog: MaterialAlertDialogBuilder? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShoppingListBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[ShoppingListViewModel::class.java]

        binding.shoppingListAppBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(context)

        setupDeleteDialog()
        handleVisibility()
        setupToolbar()
        setupViewPager()

        return binding.root
    }

    // TODO: Extract strings to XML
    private fun setupDeleteDialog() {
        deleteDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Bist du sicher?")
            .setMessage("Die Einkaufsliste löschen bedeutet, dass alle hinugefügten Rezepte gelöscht werden.")
            .setNegativeButton("Nein") { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton("Ja, löschen") { _, _ ->
                viewModel.clearRecipes()
                viewModel.ingredients.clear()
                handleVisibility()
            }

        binding.floatingActionButton.setOnClickListener {
            deleteDialog!!.show()
        }
    }

    private fun handleVisibility() {
        if (viewModel.recipes.value?.isEmpty() != false && viewModel.ingredients.isEmpty()) {
            binding.emptyLayout.visibility = View.VISIBLE
            binding.viewPager.visibility = View.GONE
            binding.floatingActionButton.visibility = View.GONE
            binding.shoppingListAppBarLayout.setExpanded(true, true)
        } else {
            binding.emptyLayout.visibility = View.GONE
            binding.viewPager.visibility = View.VISIBLE
            binding.floatingActionButton.visibility = View.VISIBLE
        }
    }

    private fun setupToolbar() {
        binding.shoppingListAppBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val isCollapsed = abs(verticalOffset) == appBarLayout.totalScrollRange
            if (isCollapsed) {
                binding.floatingActionButton.show()
            } else {
                binding.floatingActionButton.hide()
            }
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.deleteShoppingList -> {
                    deleteDialog?.show()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupViewPager() {
        val recipeListFragment = RecipeListFragment()
        val ingredientsListFragment = IngredientsListFragment()
        val adapter = ShoppingListPagerAdapter(this, listOf(recipeListFragment, ingredientsListFragment))
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout2, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.recipe)
                1 -> getString(R.string.ingredients)
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class Recipe(
    val id: String,
    val name: String,
    var portions: Int = 1
)

data class Ingredient(
    val name: String,
    val amount: String,
    var isChecked: Boolean = false
)

class ShoppingListPagerAdapter(fragment: Fragment, private val fragments: List<Fragment>) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}

class IngredientsListFragment : RecyclerViewFragment<IngredientsAdapter>() {
    // TODO: Implement arguments (checkboxes boolean and ingredients list)
    override fun provideAdapter(): IngredientsAdapter {
        val viewModel = ViewModelProvider(requireActivity())[ShoppingListViewModel::class.java]
        return IngredientsAdapter(viewModel.ingredients)
    }
}

class IngredientsAdapter(private val ingredients: List<Ingredient>) : RecyclerView.Adapter<IngredientsAdapter.IngredientViewHolder>() {

    class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ingredientCard: MaterialCardView = itemView.findViewById(R.id.ingredientCard)
        val ingredientName: MaterialTextView = itemView.findViewById(R.id.ingredientName)
        val ingredientAmount: MaterialTextView = itemView.findViewById(R.id.ingredientAmount)
        val checkBox: MaterialCheckBox = itemView.findViewById(R.id.checkBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shopping_list_ingredient, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = ingredients[position]

        val defaultColor = ContextCompat.getColor(holder.ingredientName.context, R.color.md_theme_onSurface)
        val checkedColor = ContextCompat.getColor(holder.ingredientName.context, R.color.md_theme_primary)

        holder.ingredientName.text = ingredient.name
        holder.ingredientName.paintFlags = if (ingredient.isChecked) Paint.STRIKE_THRU_TEXT_FLAG else 0
        holder.ingredientName.setTextColor(if (ingredient.isChecked) checkedColor else defaultColor)
        holder.ingredientAmount.text = ingredient.amount

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            ingredient.isChecked = isChecked
            holder.ingredientName.paintFlags = if (isChecked) Paint.STRIKE_THRU_TEXT_FLAG else 0
            holder.ingredientName.setTextColor(if (isChecked) checkedColor else defaultColor)
        }
        holder.ingredientCard.setOnClickListener { holder.checkBox.isChecked = !holder.checkBox.isChecked }
    }

    override fun getItemCount(): Int = ingredients.size
}