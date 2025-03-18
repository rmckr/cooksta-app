package com.example.cooksta_app.ui.shoppingList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cooksta_app.model.Ingredient
import com.example.cooksta_app.model.ShoppingListItem
import com.example.cooksta_app.model.ShoppingListItemBody
import com.example.cooksta_app.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShoppingListViewModel : ViewModel() {
    private val _recipes = MutableStateFlow<List<ShoppingListItem>?>(null)
    val recipes: StateFlow<List<ShoppingListItem>?> get() = _recipes

    private val _ingredients = MutableStateFlow<List<Ingredient>?>(null)
    val ingredients: StateFlow<List<Ingredient>?> get() = _ingredients

    // TODO: Toasts for request errors

    fun loadShoppingList() {
        viewModelScope.launch {
            val shoppingListResponse = RetrofitClient.apiService.getShoppingList()
            if (shoppingListResponse.isSuccessful) {
                _recipes.value = shoppingListResponse.body() ?: emptyList()
            }
        }
    }
    fun loadIngredients() {
        viewModelScope.launch {
            val ingredientsResponse = RetrofitClient.apiService.getShoppingListIngredients()
            if (ingredientsResponse.isSuccessful) {
                _ingredients.value = ingredientsResponse.body() ?: emptyList()
            }
        }
    }

    fun clear() {
        viewModelScope.launch {
            val deleteResponse = RetrofitClient.apiService.deleteShoppingList()
            if (deleteResponse.isSuccessful) {
                _recipes.value = emptyList()
                _ingredients.value = emptyList()
            }
        }
    }

    fun removeRecipe(recipeId: String) {
        viewModelScope.launch {
            val deleteResponse = RetrofitClient.apiService.deleteShoppingListItem(recipeId)
            if (deleteResponse.isSuccessful) {
                _recipes.value = deleteResponse.body() ?: emptyList()
            }
        }
    }

    fun removeMultipleRecipes(recipeIds: List<String>) {
        viewModelScope.launch {
            recipeIds.forEach { recipeId ->
                val deleteResponse = RetrofitClient.apiService.deleteShoppingListItem(recipeId)
                if (deleteResponse.isSuccessful) {
                    _recipes.value = deleteResponse.body() ?: emptyList()
                }
            }
        }
    }

    fun setRecipePortions(recipeId: String, portions: Int) {
        viewModelScope.launch {
            val postResponse = RetrofitClient.apiService.postShoppingListItem(ShoppingListItemBody(recipeId, portions))
            if (postResponse.isSuccessful) {
                _recipes.value = postResponse.body() ?: emptyList()
            }
        }
    }
}