package com.example.cooksta_app.ui.recipeList

import android.content.Context
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.Selection
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.FragmentRecyclerViewBinding
import com.example.cooksta_app.ui.shared.numberSelector.NumberSelector
import com.example.cooksta_app.ui.shoppingList.Recipe
import com.example.cooksta_app.ui.shoppingList.ShoppingListViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.textview.MaterialTextView
import java.util.Locale

class RecipeListFragment : Fragment(), RecipesAdapter.RecipeInteractionListener {

    private var _binding: FragmentRecyclerViewBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ShoppingListViewModel

    private var tracker: SelectionTracker<String>? = null

    private var actionMode: ActionMode? = null
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
            mode.menuInflater.inflate(R.menu.shopping_list_action_bar, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu?): Boolean {
            val selectionSize = tracker?.selection?.size() ?: 0
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
            tracker?.clearSelection()
        }
    }

    fun onSelectionChanged(selection: Selection<*>) {
        val selectedCount = selection.size()
        if (selectedCount > 0) {
            if (actionMode == null) {
                actionMode = requireActivity().startActionMode(actionModeCallback)
            }
            // TODO: String resource extrahieren
            actionMode?.title = "$selectedCount Rezept(e) ausgewählt"
        } else {
            actionMode?.finish()
            actionMode = null
        }
    }

    fun deleteSelectedItems() {
        val selectedRecipeIds = tracker?.selection?.toList() ?: return
        tracker!!.clearSelection()
        selectedRecipeIds.forEach { id ->
            viewModel.removeRecipe(id)
        }
    }

    override fun onSetPortions(id: String, portions: Int) {
        viewModel.setRecipePortions(id, portions)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecyclerViewBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[ShoppingListViewModel::class.java]

        val adapter = RecipesAdapter(this)
        val layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.addItemDecoration(MaterialDividerItemDecoration(requireContext(), layoutManager.orientation))

        tracker = SelectionTracker.Builder(
            "recipe-selection-id",
            binding.recyclerView,
            RecipeKeyProvider(adapter),
            RecipeItemDetailsLookup(binding.recyclerView),
            StorageStrategy.createStringStorage()
        ).build()
        tracker?.addObserver(object : SelectionTracker.SelectionObserver<String>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()
                onSelectionChanged(tracker!!.selection)
            }
        })
        adapter.tracker = tracker

        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            adapter.submitList(recipes)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tracker?.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        tracker?.onRestoreInstanceState(savedInstanceState)
    }

    class RecipeItemDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<String>() {
        override fun getItemDetails(e: MotionEvent): ItemDetails<String>? {
            val view = recyclerView.findChildViewUnder(e.x, e.y) ?: return null
            return (recyclerView.getChildViewHolder(view) as RecipesAdapter.RecipeViewHolder).getItemDetails()
        }
    }
}

class RecipesAdapter(private val interactionListener: RecipeInteractionListener) : ListAdapter<Recipe, RecipesAdapter.RecipeViewHolder>(DiffCallback()) {
    var tracker: SelectionTracker<String>? = null

    init {
        this.setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_shopping_list_recipe, viewGroup, false)
        return RecipeViewHolder(view)
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
                    is Payload.UpdatePortions -> holder.updatePortions(recipe.id, payload.portions)
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    interface RecipeInteractionListener {
        fun onSetPortions(id: String, portions: Int)
    }

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recipeCard = itemView.findViewById<MaterialCardView>(R.id.recipeCard)
        private val recipeName = itemView.findViewById<MaterialTextView>(R.id.recipeName)
        private val recipePortionsSelector = itemView.findViewById<NumberSelector>(R.id.recipePortionsSelector)

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<String> {
            return object : ItemDetailsLookup.ItemDetails<String>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): String = getItem(adapterPosition).id
            }
        }

        fun updatePortions(id: String, portions: Int) {
            interactionListener.onSetPortions(id, portions)
            recipePortionsSelector.setValue(portions)
        }

        fun updateSelection(isSelected: Boolean) {
            val defaultColor = ContextCompat.getColor(recipeCard.context, R.color.md_theme_surface)
            val selectedColor = ContextCompat.getColor(recipeCard.context, R.color.md_theme_surfaceVariant)

            recipeCard.setCardBackgroundColor(if (isSelected) selectedColor else defaultColor)
            recipeCard.isActivated = isSelected
        }

        fun bind(recipe: Recipe, isSelected: Boolean) {
            updateSelection(isSelected)

            recipeName.text = recipe.name

            recipePortionsSelector.setValue(recipe.portions)
            recipePortionsSelector.setOnValueChangeListener {
                interactionListener.onSetPortions(recipe.id, it)
            }

            recipeCard.setOnClickListener { view ->
                view.clearFocus()
                val inputMethodManager = itemView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(itemView.windowToken, 0)
            }
        }
    }

    sealed class Payload {
        data class UpdatePortions(val portions: Int) : Payload()
    }

    class DiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Recipe, newItem: Recipe): Any? {
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