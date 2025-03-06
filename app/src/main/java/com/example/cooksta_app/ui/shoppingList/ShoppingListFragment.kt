package com.example.cooksta_app.ui.shoppingList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.FragmentShoppingListBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
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

        // TODO: Display loading spinner while loading data
        viewModel.loadShoppingList()
        lifecycleScope.launch {
            viewModel.recipes.collect {
                handleVisibility()
            }
        }

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
                viewModel.clear()
                handleVisibility()
            }

        binding.floatingActionButton.setOnClickListener {
            deleteDialog!!.show()
        }
    }

    private fun handleVisibility() {
        if (viewModel.recipes.value.isEmpty() && viewModel.ingredients.value.isEmpty()) {
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

        lifecycleScope.launch {
            viewModel.ingredients.collect { ingredients ->
                ingredientsListFragment.submitIngredients(ingredients)
            }
        }

        val adapter = ShoppingListPagerAdapter(this, listOf(recipeListFragment, ingredientsListFragment))
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout2, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.recipe)
                1 -> getString(R.string.all_ingredients)
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ShoppingListPagerAdapter(fragment: Fragment, private val fragments: List<Fragment>) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}
