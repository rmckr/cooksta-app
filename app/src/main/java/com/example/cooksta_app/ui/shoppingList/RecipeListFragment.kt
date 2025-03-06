package com.example.cooksta_app.ui.shoppingList

import android.content.Context
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.Selection
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.ItemShoppingListRecipeBinding
import com.example.cooksta_app.model.ShoppingListItem
import com.example.cooksta_app.ui.shared.RecyclerViewFragment
import com.example.cooksta_app.ui.shared.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeListFragment : RecyclerViewFragment<RecipesAdapter>(), RecipesAdapter.RecipeInteractionListener {

    private val viewModel: ShoppingListViewModel by lazy {
        ViewModelProvider(requireActivity())[ShoppingListViewModel::class.java]
    }
    private lateinit var tracker: SelectionTracker<String>

    private var actionMode: ActionMode? = null
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
            mode.menuInflater.inflate(R.menu.shopping_list_action_bar, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu?): Boolean {
            val selectionSize = tracker.selection.size()
            menu?.findItem(R.id.menu_delete)?.isVisible = selectionSize > 0
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.menu_delete -> {
                    deleteSelectedItems()
                    mode.finish()
                    return true
                }
                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            tracker.clearSelection()
        }
    }

    override fun provideAdapter(): RecipesAdapter {
        return RecipesAdapter(this)
    }

    override fun onRecyclerViewCreated(recyclerView: RecyclerView) {
        tracker = SelectionTracker.Builder(
            "recipe-selection-id",
            recyclerView,
            RecipeKeyProvider(adapter),
            RecipeItemDetailsLookup(recyclerView),
            StorageStrategy.createStringStorage()
        ).build()
        tracker.addObserver(object : SelectionTracker.SelectionObserver<String>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()
                onSelectionChanged(tracker.selection)
            }
        })
        adapter.tracker = tracker

        lifecycleScope.launch {
            viewModel.recipes.collect { recipes ->
                adapter.submitList(recipes)
            }
        }
    }

    private fun onSelectionChanged(selection: Selection<*>) {
        val selectedCount = selection.size()
        if (selectedCount > 0) {
            if (actionMode == null) {
                actionMode = requireActivity().startActionMode(actionModeCallback)
            }
            actionMode?.title = resources.getQuantityString(
                R.plurals.selected_recipes_count,
                selectedCount,
                selectedCount
            )
        } else {
            actionMode?.finish()
            actionMode = null
        }
    }

    private fun deleteSelectedItems() {
        val selectedRecipeIds = tracker.selection.toList()
        tracker.clearSelection()
        selectedRecipeIds.forEach { id ->
            viewModel.removeRecipe(id)
        }
    }

    override fun onSetPortions(id: String, portions: Int) {
        viewModel.setRecipePortions(id, portions)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tracker.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        tracker.onRestoreInstanceState(savedInstanceState)
    }
}

class RecipeItemDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<String>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<String>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y) ?: return null
        return (recyclerView.getChildViewHolder(view) as RecipesAdapter.RecipeViewHolder).getItemDetails()
    }
}

class RecipesAdapter(private val interactionListener: RecipeInteractionListener) : ListAdapter<ShoppingListItem, RecipesAdapter.RecipeViewHolder>(
    DiffCallback()
) {
    var tracker: SelectionTracker<String>? = null

    init {
        this.setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.hashCode().toLong()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemShoppingListRecipeBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = getItem(position)
        holder.bind(recipe, tracker?.isSelected(recipe.id) ?: false)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int, payloads: MutableList<Any>) {
        val recipe = getItem(position)
        if (payloads.isNotEmpty()) {
            payloads.forEach { payload ->
                when (payload) {
                    "Selection-Changed" -> holder.updateSelection(tracker?.isSelected(recipe.id) ?: false)
                    is Payload.UpdatePortions -> holder.updatePortions(payload.portions)
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    interface RecipeInteractionListener {
        fun onSetPortions(id: String, portions: Int)
    }

    inner class RecipeViewHolder(private val binding: ItemShoppingListRecipeBinding) : RecyclerView.ViewHolder(binding.root) {

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<String> {
            return object : ItemDetailsLookup.ItemDetails<String>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): String = getItem(adapterPosition).id
            }
        }

        fun updatePortions(portions: Int) {
            binding.recipePortionsSelector.setValue(portions)
        }

        fun updateSelection(isSelected: Boolean) {
            val defaultColor = ContextCompat.getColor(binding.recipeCard.context, R.color.md_theme_surface)
            val selectedColor = ContextCompat.getColor(binding.recipeCard.context, R.color.md_theme_surfaceVariant)

            binding.recipeCard.setCardBackgroundColor(if (isSelected) selectedColor else defaultColor)
            binding.recipeCard.isActivated = isSelected
        }

        fun bind(recipe: ShoppingListItem, isSelected: Boolean) {
            updateSelection(isSelected)

            binding.recipeName.text = recipe.title

            binding.recipePortionsSelector.setValue(recipe.portions)
            binding.recipePortionsSelector.setOnValueChangeListener {
                interactionListener.onSetPortions(recipe.id, it)
            }

            binding.recipeCard.setOnClickListener { view ->
                view.clearFocus()
                val inputMethodManager = itemView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(itemView.windowToken, 0)
            }
            CoroutineScope(Dispatchers.IO).launch {
                val bitmap = Utilities.fetchVideoThumbnail(binding.recipePicture.context, recipe.id)
                if (bitmap != null) {
                    withContext(Dispatchers.Main) {
                        binding.recipePicture.load(bitmap)
                    }
                }
            }
        }
    }

    sealed class Payload {
        data class UpdatePortions(val portions: Int) : Payload()
    }

    class DiffCallback : DiffUtil.ItemCallback<ShoppingListItem>() {
        override fun areItemsTheSame(oldItem: ShoppingListItem, newItem: ShoppingListItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ShoppingListItem, newItem: ShoppingListItem): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: ShoppingListItem, newItem: ShoppingListItem): Any? {
            return if (oldItem.portions != newItem.portions) Payload.UpdatePortions(newItem.portions) else null
        }
    }
}

class RecipeKeyProvider(private val adapter: RecipesAdapter) : ItemKeyProvider<String>(SCOPE_CACHED) {
    override fun getKey(position: Int): String {
        return adapter.currentList[position].id
    }

    override fun getPosition(key: String): Int {
        return adapter.currentList.indexOfFirst { it.id == key }
    }
}