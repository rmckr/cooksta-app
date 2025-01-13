package com.example.cooksta_app.ui.shoppingList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ShoppingListViewModel : ViewModel() {
    private val _recipes = MutableLiveData<List<Recipe>>(
        mutableListOf(
            Recipe("spaghettiRecipe", "Spaghetti Bolognese"),
            Recipe("pizzaRecipe", "Pizza Margherita"),
            Recipe("caesarRecipe", "Caesar Salad")
        )
    )
    val recipes: LiveData<List<Recipe>> get() = _recipes

    fun clearRecipes() {
        _recipes.value = emptyList()
    }
    fun removeRecipe(id: String) {
        _recipes.value = _recipes.value?.filter { it.id != id }

    }
    fun setRecipePortions(id: String, portions: Int) {
        _recipes.value = _recipes.value?.map {
            if (it.id == id) it.copy(portions = portions.coerceAtLeast(1).coerceAtMost(999)) else it
        } ?: emptyList()
    }

    val ingredients = mutableListOf<Ingredient>(
        Ingredient("Tomaten", "3 Stück"),
        Ingredient("Käse", "200 g"),
        Ingredient("Olivenöl", "50 ml")
    )
}