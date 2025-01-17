package com.example.cooksta_app.ui.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksta_app.R
import com.example.cooksta_app.ui.shared.RecyclerViewFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

data class IngredientSelectionItem(var id: String, var name: String?, var amount: Double?)

class IngredientSelectionListFragment :
    RecyclerViewFragment<IngredientSelectionAdapter>(),
    IngredientSelectionAdapter.IngredientSelectionInteractionListener {

    private val viewModel: CreateViewModel by lazy {
        ViewModelProvider(requireActivity())[CreateViewModel::class.java]
    }

    // TODO: Get from API
    val availableIngredients = listOf(
        "Backpulver (TL)", "Butter (g)", "Eier (Stück)", "Essig (ml)", "Fisch (g)", "Hefe (g)",
        "Honig (EL)", "Hähnchen (g)", "Karotten (g)", "Kartoffeln (g)", "Knoblauch (Zehe)",
        "Kokosmilch (ml)", "Käse (g)", "Mandeln (g)", "Mehl (g)", "Milch (ml)", "Nudeln (g)",
        "Nüsse (g)", "Olivenöl (ml)", "Paprika (g)", "Reis (g)", "Rindfleisch (g)", "Sahne (ml)",
        "Schinken (g)", "Senf (EL)", "Spinat (g)", "Tomaten (g)", "Vanillezucker (TL)",
        "Wasser (ml)", "Zimt (TL)", "Zucker (g)", "Zwiebeln (g)",
    )

    override fun provideAdapter(): IngredientSelectionAdapter {
        return IngredientSelectionAdapter(
            availableIngredients = availableIngredients,
            interactionListener = this
        )
    }

    override fun provideDecoration(): RecyclerView.ItemDecoration? {
        return null
    }

    fun createIngredient() {
        viewModel.createIngredient()
    }

    override fun onIngredientChanged(position: Int, name: String?, amount: Double?) {
        viewModel.updateIngredient(position, name, amount)
    }

    override fun onIngredientDeleted(position: Int, fromZeroAmount: Boolean) {
        val message = if (fromZeroAmount)
            "Wenn du die Menge einer Zutat auf \"0\" setzt, wird sie gelöscht." else
                "Durch das Löschen einer Zutat wird diese aus der Liste entfernt."
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Bist du sicher?")
            .setMessage(message)
            .setNegativeButton("Nein") { dialog, _ -> dialog.cancel() }
            .setPositiveButton("Ja, löschen") { _, _ -> viewModel.deleteIngredient(position) }
            .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.ingredientSelection.collect { ingredientSelection ->
                if (adapter.currentList != ingredientSelection) {
                    adapter.submitList(ingredientSelection)
                }
            }
        }
    }
}

class IngredientSelectionAdapter(
    private val availableIngredients: List<String>,
    private val interactionListener: IngredientSelectionInteractionListener
) : ListAdapter<IngredientSelectionItem, IngredientSelectionAdapter.IngredientSelectionViewHolder>(IngredientSelectionDiffCallback()) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): IngredientSelectionViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_ingredient_selection_ingredient, viewGroup, false)
        return IngredientSelectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientSelectionViewHolder, position: Int) {
        val ingredient = getItem(position)
        holder.bind(ingredient)
    }

    override fun onBindViewHolder(holder: IngredientSelectionViewHolder, position: Int, payloads: MutableList<Any>) {
        val ingredient = getItem(position)
        if (payloads.isNotEmpty()) {
            payloads.forEach { payload ->
                if (payload is Payload.IngredientChangePayload) {
                    when (payload.changedField) {
                        "name" -> holder.updateIngredientName(ingredient.name)
                        "amount" -> holder.updateIngredientAmount(ingredient.amount)
                    }
                }
            }
        } else {
            holder.bind(ingredient)
        }
    }

    sealed class Payload {
        data class IngredientChangePayload(val changedField: String)
    }

    class IngredientSelectionDiffCallback : DiffUtil.ItemCallback<IngredientSelectionItem>() {
        override fun areItemsTheSame(oldItem: IngredientSelectionItem, newItem: IngredientSelectionItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: IngredientSelectionItem, newItem: IngredientSelectionItem): Boolean {
            return oldItem.name == newItem.name && oldItem.amount == newItem.amount
        }

        override fun getChangePayload(oldItem: IngredientSelectionItem, newItem: IngredientSelectionItem): Any? {
            val payloads = mutableListOf<Payload.IngredientChangePayload>()
            if (oldItem.name != newItem.name) payloads.add(Payload.IngredientChangePayload("name"))
            if (oldItem.amount != newItem.amount) payloads.add(Payload.IngredientChangePayload("amount"))
            return if (payloads.isNotEmpty()) payloads else null
        }
    }

    interface IngredientSelectionInteractionListener {
        fun onIngredientChanged(position: Int, name: String?, amount: Double?)
        fun onIngredientDeleted(position: Int, fromZeroAmount: Boolean)
    }

    inner class IngredientSelectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ingredientNameLayout = itemView.findViewById<TextInputLayout>(R.id.ingredientNameLayout)
        private val ingredientName = itemView.findViewById<MaterialAutoCompleteTextView>(R.id.ingredientName)
        private val ingredientAmountLayout = itemView.findViewById<TextInputLayout>(R.id.ingredientAmountLayout)
        private val ingredientAmount = itemView.findViewById<EditText>(R.id.ingredientAmount)
        private val ingredientDeleteButton = itemView.findViewById<MaterialButton>(R.id.ingredientDeleteButton)

        val currentIngredientNames get() = currentList.map { it.name }

        fun updateIngredientName(name: String?) {
            if (ingredientName.text.toString() != name) {
                ingredientName.setText(name, false)
            }
        }

        fun updateIngredientAmount(amount: Double?) {
            if (ingredientAmount.text.toString() != amount.toString()) {
                ingredientAmount.setText(amount?.toString())
            }
        }

        fun bind(ingredient: IngredientSelectionItem) {
            updateIngredientName(ingredient.name)
            updateIngredientAmount(ingredient.amount)

            val availableOptions = availableIngredients.filterNot { currentIngredientNames.contains(it) }
            val arrayAdapter = ArrayAdapter(itemView.context, android.R.layout.simple_dropdown_item_1line, availableOptions)
            ingredientName.setAdapter(arrayAdapter)

            val parsedUnit = parseUnit(ingredient.name ?: "")
            if (ingredient.name != null && parsedUnit == null) {
                ingredientNameLayout.error = "Format: \"Zutat (Einheit)\""
            } else {
                ingredientAmountLayout.suffixText = parsedUnit
            }

            ingredientName.setOnItemClickListener { _, _, _, _ ->
                ingredientAmountLayout.requestFocus()
                submitIngredientName()
            }
            ingredientName.setOnEditorActionListener { view, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    view.clearFocus()
                    submitIngredientName()
                }
                false
            }
            ingredientAmount.setOnEditorActionListener { view, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    view.clearFocus()
                    submitIngredientAmount()
                }
                false
            }

            ingredientDeleteButton.setOnClickListener {
                interactionListener.onIngredientDeleted(adapterPosition, false)
            }
        }

        private fun parseUnit(input: String): String? {
            val regex = Regex("""\((.+)\)""")
            val matchResult = regex.find(input)
            return matchResult?.groupValues?.get(1)
        }

        private fun submitIngredientName() {
            val name = ingredientName.text.toString()
            val parsedUnit = parseUnit(name)

            ingredientNameLayout.error = null
            ingredientAmountLayout.error = null
            ingredientAmountLayout.suffixText = parsedUnit

            if (parsedUnit == null) {
                ingredientNameLayout.error = "Format: \"Zutat (Einheit)\""
                return
            }
            if (currentIngredientNames.contains(name)) {
                ingredientNameLayout.error = "Zutat bereits vorhanden"
                return
            }

            interactionListener.onIngredientChanged(adapterPosition, name, null)
            updateIngredientName(name)
        }

        private fun submitIngredientAmount() {
            val amount = ingredientAmount.text.toString().toDoubleOrNull()

            if (amount == null || amount < 0) {
                ingredientAmountLayout.error = "Menge > 0"
                return
            }
            if (amount == 0.0) {
                interactionListener.onIngredientDeleted(adapterPosition, true)
                return
            }

            interactionListener.onIngredientChanged(adapterPosition, null, amount)
            updateIngredientAmount(amount)
        }
    }
}
