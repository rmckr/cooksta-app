package com.example.cooksta_app.ui.create

import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.FragmentRecyclerViewBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout

data class IngredientSelectionItem(var name: String, var amount: Double?)

class IngredientSelectionListFragment : Fragment(),
    IngredientSelectionAdapter.IngredientSelectionInteractionListener {

    private var _binding: FragmentRecyclerViewBinding? = null
    private val binding get() = _binding!!

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecyclerViewBinding.inflate(inflater, container, false)

        ingredientAdapter = IngredientSelectionAdapter(
            ingredients = selectedIngredients,
            availableIngredients = availableIngredients,
            interactionListener = this
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = ingredientAdapter

        return binding.root
    }

    fun createIngredient() {
        selectedIngredients.add(IngredientSelectionItem(name = "", amount = null))
        binding.recyclerView.post {
            ingredientAdapter.notifyItemInserted(selectedIngredients.size - 1)
        }
    }

    override fun onAmountChanged(position: Int, amount: Double) {
        selectedIngredients[position].amount = amount
        binding.recyclerView.post {
            ingredientAdapter.notifyItemChanged(position)
        }
        Log.d("Test", "onAmountChanged: $selectedIngredients")
    }

    override fun onIngredientChanged(position: Int, ingredient: String) {
        selectedIngredients[position].name = ingredient
        binding.recyclerView.post {
            ingredientAdapter.notifyItemChanged(position)
        }
        Log.d("Test", "onIngredientAdded: $selectedIngredients")
    }

    override fun onIngredientDeleted(position: Int) {
        Log.d("Test", "onIngredientDeleted: $selectedIngredients")
        selectedIngredients.removeAt(position)
        binding.recyclerView.post {
            ingredientAdapter.notifyItemRemoved(position)
        }
        Log.d("Test", "onIngredientDeleted: $selectedIngredients")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        fun onIngredientChanged(position: Int, ingredient: String)
        fun onAmountChanged(position: Int, amount: Double)
        fun onIngredientDeleted(position: Int)
    }

    inner class IngredientSelectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ingredientNameLayout = itemView.findViewById<TextInputLayout>(R.id.ingredientNameLayout)
        private val ingredientName = itemView.findViewById<MaterialAutoCompleteTextView>(R.id.ingredientName)
        private val ingredientAmountLayout = itemView.findViewById<TextInputLayout>(R.id.ingredientAmountLayout)
        private val ingredientAmount = itemView.findViewById<EditText>(R.id.ingredientAmount)
        private val ingredientDeleteButton = itemView.findViewById<MaterialButton>(R.id.ingredientDeleteButton)

        private var ingredientNameWatcher: TextWatcher? = null
        private var ingredientAmountWatcher: TextWatcher? = null

        fun bind(ingredient: IngredientSelectionItem) {
            ingredientName.removeTextChangedListener(ingredientNameWatcher)
            ingredientAmount.removeTextChangedListener(ingredientAmountWatcher)

            val currentIngredientNames = ingredients.map { it.name }
            val availableOptions = availableIngredients.filterNot { currentIngredientNames.contains(it) }

            fun parseUnit(input: String): String? {
                val regex = Regex("""\((.+)\)""")
                val matchResult = regex.find(input)
                return matchResult?.groupValues?.get(1)
            }

            fun changeIngredient() {
                val ingredientInputText = ingredientName.text.toString()
                val parsedUnit = parseUnit(ingredientInputText)
                ingredientAmountLayout.suffixText = parsedUnit
                if (currentIngredientNames.contains(ingredientInputText)) {
                    ingredientNameLayout.error = "Zutat existiert bereits"
                    return
                }
                if (parsedUnit == null) {
                    ingredientNameLayout.error = "Format: \"Zutat (Einheit)\""
                    return
                }
                ingredientNameLayout.error = null
                interactionListener.onIngredientChanged(adapterPosition, ingredientInputText)
            }

            val adapter = ArrayAdapter(itemView.context, android.R.layout.simple_dropdown_item_1line, availableOptions)
            ingredientName.setAdapter(adapter)

            ingredientName.setText(ingredient.name, false)
            ingredientAmount.setText(ingredient.amount?.toString())

            ingredientName.setOnItemClickListener { _, _, _, _ ->
                changeIngredient()
            }
            ingredientNameWatcher = ingredientName.addTextChangedListener {
                changeIngredient()
            }
            ingredientAmountWatcher = ingredientAmount.addTextChangedListener {
                val newAmount = it.toString().toDoubleOrNull()
                if (newAmount == null || newAmount == 0.0) {
                    interactionListener.onIngredientDeleted(adapterPosition)
                } else if (newAmount < 0.0) {
                    ingredientAmountLayout.error = "Menge > 0"
                } else {
                    ingredientAmountLayout.error = null
                    interactionListener.onAmountChanged(adapterPosition, newAmount)
                }
            }
            ingredientDeleteButton.setOnClickListener {
                interactionListener.onIngredientDeleted(adapterPosition)
            }
        }
    }
}
