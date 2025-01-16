package com.example.cooksta_app.ui.create

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksta_app.R
import com.example.cooksta_app.ui.shared.RecyclerViewFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout

data class IngredientSelectionItem(var name: String, var amount: Double?)

class IngredientSelectionListFragment :
    RecyclerViewFragment<IngredientSelectionAdapter>(),
    IngredientSelectionAdapter.IngredientSelectionInteractionListener {

    private lateinit var ingredientAdapter: IngredientSelectionAdapter

    // TODO: Get from API
    val availableIngredients = listOf(
        "Backpulver (TL)",
        "Butter (g)",
        "Eier (Stück)",
        "Essig (ml)",
        "Fisch (g)",
        "Hefe (g)",
        "Honig (EL)",
        "Hähnchen (g)",
        "Karotten (g)",
        "Kartoffeln (g)",
        "Knoblauch (Zehe)",
        "Kokosmilch (ml)",
        "Käse (g)",
        "Mandeln (g)",
        "Mehl (g)",
        "Milch (ml)",
        "Nudeln (g)",
        "Nüsse (g)",
        "Olivenöl (ml)",
        "Paprika (g)",
        "Reis (g)",
        "Rindfleisch (g)",
        "Sahne (ml)",
        "Schinken (g)",
        "Senf (EL)",
        "Spinat (g)",
        "Tomaten (g)",
        "Vanillezucker (TL)",
        "Wasser (ml)",
        "Zimt (TL)",
        "Zucker (g)",
        "Zwiebeln (g)",
    )
    val selectedIngredients = mutableListOf<IngredientSelectionItem>()

    override fun provideAdapter(): IngredientSelectionAdapter {
        ingredientAdapter = IngredientSelectionAdapter(
            ingredients = selectedIngredients,
            availableIngredients = availableIngredients,
            interactionListener = this
        )
        return ingredientAdapter
    }

    override fun provideDecoration(): RecyclerView.ItemDecoration? {
        return null
    }

    fun createIngredient() {
        selectedIngredients.add(IngredientSelectionItem(name = "", amount = null))
        recyclerView.post {
            ingredientAdapter.notifyItemInserted(selectedIngredients.size - 1)
        }
    }

    override fun onIngredientNameChanged(position: Int, name: String) {
        selectedIngredients[position].name = name
    }

    override fun onIngredientAmountChanged(position: Int, amount: Double) {
        selectedIngredients[position].amount = amount
    }

    override fun onIngredientDeleted(position: Int, fromZeroAmount: Boolean) {
        val message = if (fromZeroAmount)
            "Wenn du die Menge einer Zutat auf \"0\" setzt, wird sie gelöscht." else
                "Durch das Löschen einer Zutat wird diese aus der Liste entfernt."
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Bist du sicher?")
            .setMessage(message)
            .setNegativeButton("Nein") { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton("Ja, löschen") { _, _ ->
                selectedIngredients.removeAt(position)
                recyclerView.post {
                    ingredientAdapter.notifyItemRemoved(position)
                }
            }
            .show()
    }
}

class IngredientSelectionAdapter(
    private val ingredients: List<IngredientSelectionItem>,
    private val availableIngredients: List<String>,
    private val interactionListener: IngredientSelectionInteractionListener
) : RecyclerView.Adapter<IngredientSelectionAdapter.IngredientSelectionViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): IngredientSelectionViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_ingredient_selection_ingredient, viewGroup, false)
        return IngredientSelectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientSelectionViewHolder, position: Int) {
        val ingredient = ingredients[position]
        holder.bind(ingredient)
    }

    override fun getItemCount() = ingredients.size

    interface IngredientSelectionInteractionListener {
        fun onIngredientNameChanged(position: Int, name: String)
        fun onIngredientAmountChanged(position: Int, amount: Double)
        fun onIngredientDeleted(position: Int, fromZeroAmount: Boolean)
    }

    inner class IngredientSelectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ingredientNameLayout = itemView.findViewById<TextInputLayout>(R.id.ingredientNameLayout)
        private val ingredientName = itemView.findViewById<MaterialAutoCompleteTextView>(R.id.ingredientName)
        private val ingredientAmountLayout = itemView.findViewById<TextInputLayout>(R.id.ingredientAmountLayout)
        private val ingredientAmount = itemView.findViewById<EditText>(R.id.ingredientAmount)
        private val ingredientDeleteButton = itemView.findViewById<MaterialButton>(R.id.ingredientDeleteButton)

        private fun updateIngredientName(name: String) {
            ingredientName.setText(name, false)
        }

        private fun updateIngredientAmount(amount: Double) {
            ingredientAmount.setText(amount.toString())
        }

        fun bind(ingredient: IngredientSelectionItem) {
            val currentIngredientNames = ingredients.map { it.name }
            val availableOptions = availableIngredients.filterNot { currentIngredientNames.contains(it) }

            fun parseUnit(input: String): String? {
                val regex = Regex("""\((.+)\)""")
                val matchResult = regex.find(input)
                return matchResult?.groupValues?.get(1)
            }

            fun submitIngredientName() {
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

                interactionListener.onIngredientNameChanged(adapterPosition, name)
                updateIngredientName(name)
            }

            fun submitIngredientAmount() {
                val amount = ingredientAmount.text.toString().toDoubleOrNull()

                if (amount == null || amount < 0) {
                    ingredientAmountLayout.error = "Menge > 0"
                    return
                }
                if (amount == 0.0) {
                    // TODO: Maybe add dialog
                    interactionListener.onIngredientDeleted(adapterPosition, true)
                    return
                }

                interactionListener.onIngredientAmountChanged(adapterPosition, amount)
                updateIngredientAmount(amount)
            }

            val adapter = ArrayAdapter(itemView.context, android.R.layout.simple_dropdown_item_1line, availableOptions)
            ingredientName.setAdapter(adapter)

            updateIngredientName(ingredient.name)
            ingredient.amount?.let { updateIngredientAmount(it) }

            val parsedUnit = parseUnit(ingredient.name)
            if (ingredient.name.isNotEmpty() && parsedUnit == null) {
                ingredientNameLayout.error = "Format: \"Zutat (Einheit)\""
            } else {
                ingredientAmountLayout.suffixText = parsedUnit
            }

            ingredientName.setOnItemClickListener { _, _, _, _ ->
                ingredientNameLayout.clearFocus()
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
    }
}
