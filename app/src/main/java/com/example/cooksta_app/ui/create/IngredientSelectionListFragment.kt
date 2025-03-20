package com.example.cooksta_app.ui.create

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksta_app.R
import com.example.cooksta_app.databinding.ItemIngredientSelectionIngredientBinding
import com.example.cooksta_app.ui.shared.RecyclerViewFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.util.Locale

data class IngredientSelectionItem(var id: String, var name: String?, var amount: Double?)

class IngredientSelectionListFragment : RecyclerViewFragment<IngredientSelectionListFragment.Adapter>(), IngredientSelectionInteractionListener {

    private val viewModel: CreateViewModel by lazy {
        ViewModelProvider(requireActivity())[CreateViewModel::class.java]
    }

    // TODO: Get from API
    private val availableIngredients = listOf(
        "Backpulver (TL)", "Butter (g)", "Eier (Stück)", "Essig (ml)", "Fisch (g)", "Hefe (g)",
        "Honig (EL)", "Hähnchen (g)", "Karotten (g)", "Kartoffeln (g)", "Knoblauch (Zehe)",
        "Kokosmilch (ml)", "Käse (g)", "Mandeln (g)", "Mehl (g)", "Milch (ml)", "Nudeln (g)",
        "Nüsse (g)", "Olivenöl (ml)", "Paprika (g)", "Reis (g)", "Rindfleisch (g)", "Sahne (ml)",
        "Schinken (g)", "Senf (EL)", "Spinat (g)", "Tomaten (g)", "Vanillezucker (TL)",
        "Wasser (ml)", "Zimt (TL)", "Zucker (g)", "Zwiebeln (g)",
    )

    override fun provideAdapter(): Adapter {
        return Adapter(
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
            getString(R.string.message_option_no1) else
                getString(R.string.message_option_no2)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.title_dialog_delete))
            .setMessage(message)
            .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.cancel() }
            .setPositiveButton(getString(R.string.delete)) { _, _ -> viewModel.deleteIngredient(position) }
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

    override fun onStop() {
        super.onStop()
        viewModel.clearIngredients()
    }

    sealed class Payload {
        data class IngredientChangePayload(val changedField: String)
    }

    class Adapter(
        private val availableIngredients: List<String>,
        private val interactionListener: IngredientSelectionInteractionListener
    ) : ListAdapter<IngredientSelectionItem, Adapter.IngredientViewHolder>(IngredientSelectionDiffCallback()) {
        private val currentIngredientNames get() = currentList.map { it.name }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): IngredientViewHolder {
            val binding = ItemIngredientSelectionIngredientBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            return IngredientViewHolder(binding)
        }

        override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
            val ingredient = getItem(position)
            holder.bind(ingredient)
        }

        override fun onBindViewHolder(holder: IngredientViewHolder, position: Int, payloads: MutableList<Any>) {
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

        inner class IngredientViewHolder(private val binding: ItemIngredientSelectionIngredientBinding) : RecyclerView.ViewHolder(binding.root) {

            val ingredientNameLayout get() = binding.ingredientNameLayout

            fun updateIngredientName(name: String?) {
                if (binding.ingredientName.text.toString() != name) {
                    binding.ingredientName.setText(name, false)
                }
            }

            fun updateIngredientAmount(amount: Double?) {
                if (amount == null) {
                    binding.ingredientAmount.text = null
                } else if (binding.ingredientAmount.text.toString() != amount.toString()) {
                    binding.ingredientAmount.setText(String.format(Locale.getDefault(), "%.2f", amount))
                }
            }

            fun bind(ingredient: IngredientSelectionItem) {
                updateIngredientName(ingredient.name)
                updateIngredientAmount(ingredient.amount)

                val availableOptions = availableIngredients.filterNot { currentIngredientNames.contains(it) }
                val arrayAdapter = ArrayAdapter(itemView.context, android.R.layout.simple_dropdown_item_1line, availableOptions)
                binding.ingredientName.setAdapter(arrayAdapter)

                val parsedUnit = parseUnit(ingredient.name ?: "")
                if (ingredient.name != null && parsedUnit == null) {
                    binding.ingredientNameLayout.error = "Format: \"Zutat (Einheit)\""
                } else {
                    binding.ingredientAmountLayout.suffixText = parsedUnit
                }

                val handler = Handler(Looper.getMainLooper())
                var workRunnableName: Runnable? = null
                var workRunnableAmount: Runnable? = null

                binding.ingredientName.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        workRunnableName?.let { handler.removeCallbacks(it) }
                        workRunnableName = Runnable {
                            val name = s.toString().trim()
                            if (name.isNotEmpty()) {
                                submitIngredientName()

                                if (!binding.ingredientAmount.text.isNullOrEmpty()) {
                                    binding.ingredientAmount.requestFocus()
                                }
                            }
                        }
                        handler.postDelayed(workRunnableName!!, 1500)
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })

                binding.ingredientAmount.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        workRunnableAmount?.let { handler.removeCallbacks(it) }
                        workRunnableAmount = Runnable {
                            val amount = s.toString().trim().toDoubleOrNull()
                            if (amount != null) {
                                submitIngredientAmount()

                                binding.ingredientAmount.clearFocus()

                                val imm = binding.root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.hideSoftInputFromWindow(binding.ingredientAmount.windowToken, 0)
                            }
                        }
                        handler.postDelayed(workRunnableAmount!!, 1500)
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })

                binding.ingredientAmount.setOnEditorActionListener { view, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        submitIngredientAmount()

                        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(view.windowToken, 0)

                        view.clearFocus()
                        view.postDelayed({ view.isFocusableInTouchMode = false }, 100)

                        true
                    } else {
                        false
                    }
                }

                binding.ingredientDeleteButton.setOnClickListener {
                    interactionListener.onIngredientDeleted(bindingAdapterPosition, false)
                }
            }

            private fun parseUnit(input: String): String? {
                val regex = Regex("""\((.+)\)""")
                val matchResult = regex.find(input)
                return matchResult?.groupValues?.get(1)
            }

            private fun submitIngredientName() {
                val name = binding.ingredientName.text.toString()
                val parsedUnit = parseUnit(name)

                binding.ingredientNameLayout.error = null
                binding.ingredientAmountLayout.error = null
                binding.ingredientAmountLayout.suffixText = parsedUnit

                if (parsedUnit == null) {
                    binding.ingredientNameLayout.error = "Format: \"Zutat (Einheit)\""
                    return
                }

                interactionListener.onIngredientChanged(bindingAdapterPosition, name, null)
                updateIngredientName(name)
            }

            private fun submitIngredientAmount() {
                val amount = binding.ingredientAmount.text.toString().toDoubleOrNull()

                if (amount == 0.0) {
                    interactionListener.onIngredientDeleted(bindingAdapterPosition, true)
                    return
                }

                interactionListener.onIngredientChanged(bindingAdapterPosition, null, amount)
                updateIngredientAmount(amount)
            }
        }
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
            return payloads.ifEmpty { null }
        }
    }
}

interface IngredientSelectionInteractionListener {
    fun onIngredientChanged(position: Int, name: String?, amount: Double?)
    fun onIngredientDeleted(position: Int, fromZeroAmount: Boolean)
}