package com.example.cooksta_app.ui.shoppingList

import androidx.lifecycle.ViewModel

class ShoppingListViewModel : ViewModel() {
    val recipes = mutableListOf<Recipe>(
        Recipe("Spaghetti Bolognese"),
        Recipe("Pizza Margherita"),
        Recipe("Caesar Salad")
    )
    val ingredients = mutableListOf<Ingredient>(
        Ingredient("Tomaten", "3 Stück"),
        Ingredient("Käse", "200 g"),
        Ingredient("Olivenöl", "50 ml")
    )
}