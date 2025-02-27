package com.example.cooksta_app.ui.create

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class CreateViewModel : ViewModel() {

    private val _portions = MutableStateFlow<Int>(1)
    val portions: StateFlow<Int> = _portions

    private val _ingredientSelection = MutableStateFlow<List<IngredientSelectionItem>>(emptyList())
    val ingredientSelection: StateFlow<List<IngredientSelectionItem>> get() = _ingredientSelection

    fun setPortions(value: Int) {
        _portions.value = value
    }

    val ingredientSelectionCount get() = _ingredientSelection.value.size

    fun createIngredient() {
        _ingredientSelection.value = _ingredientSelection.value.toMutableList().apply {
            add(IngredientSelectionItem(id = UUID.randomUUID().toString(), name = null, amount = null))
        }
    }
    fun updateIngredient(position: Int, name: String? = null, amount: Double? = null) {
        _ingredientSelection.value = _ingredientSelection.value.mapIndexed { index, item ->
            if (index == position) {
                item.copy(name = name ?: item.name, amount = amount ?: item.amount)
            } else item
        }
    }
    fun deleteIngredient(position: Int) {
        _ingredientSelection.value = _ingredientSelection.value.toMutableList().apply {
            removeAt(position)
        }
    }
    fun clearIngredients() {
        _ingredientSelection.value = emptyList() // Zutatenliste leeren
    }
}