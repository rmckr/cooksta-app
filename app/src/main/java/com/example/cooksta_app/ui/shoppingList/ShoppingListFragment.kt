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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.math.abs

class ShoppingListFragment : Fragment() {

    private var _binding: FragmentShoppingListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ShoppingListViewModel
    private var deleteDialog: MaterialAlertDialogBuilder? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentShoppingListBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(requireActivity())[ShoppingListViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadShoppingList()
        viewModel.loadIngredients()
        lifecycleScope.launch {
            combine(viewModel.recipes, viewModel.ingredients) { recipes, ingredients ->
                listOf(recipes, ingredients)
            }.collect {
                handleVisibility()
            }
        }

        binding.shoppingListAppBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(context)

        setupDeleteDialog()
        setupToolbar()
        setupViewPager()
    }

    private fun setupDeleteDialog() {
        deleteDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.title_dialog_delete))
            .setMessage(getString(R.string.message_dialog_delete_shoppingList))
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                viewModel.clear()
            }

        binding.floatingActionButton.setOnClickListener {
            deleteDialog!!.show()
        }
    }

    private fun handleVisibility() {
        if (viewModel.recipes.value == null || viewModel.ingredients.value == null) {
            binding.loadingSpinnerLayout.visibility = View.VISIBLE
            binding.emptyLayout.visibility = View.GONE
            binding.viewPager.visibility = View.GONE
            binding.floatingActionButton.visibility = View.GONE
            binding.shoppingListAppBarLayout.setExpanded(true, true)
        } else if (viewModel.recipes.value!!.isEmpty() || viewModel.ingredients.value!!.isEmpty()) {
            binding.loadingSpinnerLayout.visibility = View.GONE
            binding.emptyLayout.visibility = View.VISIBLE
            binding.viewPager.visibility = View.GONE
            binding.floatingActionButton.visibility = View.GONE
            binding.shoppingListAppBarLayout.setExpanded(true, true)
        } else {
            binding.loadingSpinnerLayout.visibility = View.GONE
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
            viewModel.ingredients.collect {
                ingredientsListFragment.submitIngredients(it)
            }
        }

        val adapter = ShoppingListPagerAdapter(listOf(recipeListFragment, ingredientsListFragment))
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

    inner class ShoppingListPagerAdapter(private val fragments: List<Fragment>) : FragmentStateAdapter(this) {
        override fun getItemCount() = fragments.size

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }
}
