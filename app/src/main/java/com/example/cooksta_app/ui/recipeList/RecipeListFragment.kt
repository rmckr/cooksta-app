package com.example.cooksta_app.ui.recipeList

import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
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
            actionMode?.title = "$selectedCount Rezept(e) ausgewählt"
        } else {
            actionMode?.finish()
            actionMode = null
        }
    }

    fun deleteSelectedItems() {
        val selectedIdHashes = tracker?.selection?.toList()?.sortedDescending() ?: return
        tracker!!.clearSelection()
        selectedIdHashes.forEach { idHash ->
            viewModel.removeRecipe(idHash)
        }
    }

    override fun onSetPortions(id: String, portions: Int) {
        Log.d("RecipeListFragment", "onSetPortions: $portions")
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
                Log.d("RecipeListFragment", "onSelectionChanged: ${tracker!!.selection}")
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
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_recipe, viewGroup, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = getItem(position)
        holder.bind(recipe, tracker?.isSelected(recipe.id) ?: false)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val payload = payloads[0] as? Int
            if (payload != null) {
                holder.updatePortions(payload)
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    interface RecipeInteractionListener {
        fun onSetPortions(id: String, portions: Int)
    }

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<String> {
            return object : ItemDetailsLookup.ItemDetails<String>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): String = getItem(adapterPosition).id
            }
        }

        fun updatePortions(portions: Int) {
            val recipePortions = itemView.findViewById<EditText>(R.id.recipePortions)
            recipePortions.setText(String.format(Locale.getDefault(), "%d", portions))
        }

        fun bind(recipe: Recipe, isSelected: Boolean) {
            val recipeCard = itemView.findViewById<MaterialCardView>(R.id.recipeCard)
            val recipeName = itemView.findViewById<MaterialTextView>(R.id.recipeName)
            val recipePortions = itemView.findViewById<EditText>(R.id.recipePortions)
            val decButton = itemView.findViewById<MaterialButton>(R.id.recipePortionsDecButton)
            val incButton = itemView.findViewById<MaterialButton>(R.id.recipePortionsIncButton)

            fun setPortions(amount: Int) {
                val newAmount = amount.coerceAtLeast(1).coerceAtMost(999)
                interactionListener.onSetPortions(recipe.id, newAmount)
                recipePortions.setText(String.format(Locale.getDefault(), "%d", newAmount))
            }

            val defaultColor = ContextCompat.getColor(recipeCard.context, R.color.md_theme_surface)
            val selectedColor = ContextCompat.getColor(recipeCard.context, R.color.md_theme_surfaceVariant)

            recipeCard.setBackgroundColor(if (isSelected) selectedColor else defaultColor)
            recipeCard.isActivated = isSelected

            recipeName.text = recipe.name
            recipePortions.setText(String.format(Locale.getDefault(), "%d", recipe.portions))

            recipePortions.setOnFocusChangeListener { view, hasFocus ->
                view as EditText
                if (hasFocus) {
                    view.post { view.selectAll() }
                } else {
                    view.post {
                        view.setSelection(0, 0)
                        view.isCursorVisible = false
                    }
                }
            }
            recipePortions.setOnClickListener { view ->
                view as EditText
                view.selectAll()
            }
            recipePortions.setOnEditorActionListener { view, actionId, _ ->
                view as EditText
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val inputText = recipePortions.text.toString()
                    setPortions(inputText.toIntOrNull() ?: 0)
                    view.isCursorVisible = false
                }
                false
            }
            decButton.setOnClickListener {
                setPortions(recipe.portions - 1)
                recipePortions.clearFocus()
            }
            incButton.setOnClickListener {
                setPortions(recipe.portions + 1)
                recipePortions.clearFocus()
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Recipe, newItem: Recipe): Any? {
            return if (oldItem.portions != newItem.portions) newItem.portions else null
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