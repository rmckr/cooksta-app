package com.example.cooksta_app.ui.shoppingList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.FragmentShoppingListBinding
import com.example.cooksta_app.ui.recyclerView.RecyclerViewFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.math.abs

class ShoppingListFragment : Fragment() {

    private var _binding: FragmentShoppingListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ShoppingListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShoppingListBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ShoppingListViewModel::class.java]

        handleVisibility()
        setupToolbar()
        setupViewPager()

        return binding.root
    }

    private fun handleVisibility() {
        if (viewModel.recipes.isEmpty() && viewModel.ingredients.isEmpty()) {
            binding.emptyLayout.visibility = View.VISIBLE
            binding.viewPager.visibility = View.GONE
            binding.floatingActionButton.visibility = View.GONE
            binding.appBarLayout.setExpanded(true, true)
        } else {
            binding.emptyLayout.visibility = View.GONE
            binding.viewPager.visibility = View.VISIBLE
            binding.floatingActionButton.visibility = View.VISIBLE
        }
    }

    private fun setupToolbar() {
        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val isCollapsed = abs(verticalOffset) == appBarLayout.totalScrollRange
            if (isCollapsed) {
                binding.floatingActionButton.show()
            } else {
                binding.floatingActionButton.hide()
            }
        }

        binding.floatingActionButton.setOnClickListener {
            handleDelete()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.deleteShoppingList -> {
                    handleDelete()
                    true
                }
                else -> false
            }
        }
    }

    private fun handleDelete() {
        viewModel.recipes.clear()
        viewModel.ingredients.clear()
        handleVisibility()
    }

    private fun setupViewPager() {
        val adapter = ShoppingListPagerAdapter(this, listOf(RecipeListFragment(), IngredientsListFragment()))
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
    val name: String
)

data class Ingredient(
    val name: String,
    val amount: String
)

class ShoppingListPagerAdapter(fragment: Fragment, private val fragments: List<Fragment>) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}

class RecipeListFragment: RecyclerViewFragment<RecipesAdapter>() {
    override fun provideAdapter(): RecipesAdapter {
        val viewModel = ViewModelProvider(this)[ShoppingListViewModel::class.java]
        return RecipesAdapter(viewModel.recipes)
    }
}

class IngredientsListFragment: RecyclerViewFragment<IngredientsAdapter>() {
    override fun provideAdapter(): IngredientsAdapter {
        val viewModel = ViewModelProvider(this)[ShoppingListViewModel::class.java]
        return IngredientsAdapter(viewModel.ingredients)
    }
}

class RecipesAdapter(private val recipes: List<Recipe>) : RecyclerView.Adapter<RecipesAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeView: TextView = itemView.findViewById(R.id.recipeName)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_recipe, viewGroup, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.recipeView.text = recipe.name
    }

    override fun getItemCount(): Int = recipes.size
}

class IngredientsAdapter(private val ingredients: List<Ingredient>) : RecyclerView.Adapter<IngredientsAdapter.IngredientViewHolder>() {

    class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ingredientName: TextView = itemView.findViewById(R.id.ingredientName)
        val ingredientAmount: TextView = itemView.findViewById(R.id.ingredientAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ingredient, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = ingredients[position]
        holder.ingredientName.text = ingredient.name
        holder.ingredientAmount.text = ingredient.amount
    }

    override fun getItemCount(): Int = ingredients.size
}