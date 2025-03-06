package com.example.cooksta_app.ui.shoppingList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cooksta_app.model.Ingredient
import com.example.cooksta_app.model.PartialUser
import com.example.cooksta_app.model.ShoppingListItem
import com.example.cooksta_app.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShoppingListViewModel : ViewModel() {
    private val _recipes = MutableStateFlow<List<ShoppingListItem>>(emptyList())
    val recipes: StateFlow<List<ShoppingListItem>> get() = _recipes

    private val _ingredients = MutableStateFlow<List<Ingredient>>(emptyList())
    val ingredients: StateFlow<List<Ingredient>> get() = _ingredients

    private fun sendUpdateAndReload() {
        viewModelScope.launch {
            val patchResponse = RetrofitClient.apiService.patchUser("me", PartialUser(
                shoppingList = recipes.value.associate { recipe -> recipe.id to recipe.portions }
            ))
            if (patchResponse.isSuccessful) {
                loadShoppingList()
            }
        }
    }

    fun loadShoppingList() {
        viewModelScope.launch {
            val userResponse = RetrofitClient.apiService.getShoppingList("me")
            if (userResponse.isSuccessful) {
                _recipes.value = userResponse.body()?.shoppingList ?: emptyList()
                _ingredients.value = userResponse.body()?.ingredients ?: emptyList()
            }
        }
    }

    fun clear() {
        _recipes.value = emptyList()
        _ingredients.value = emptyList()
        sendUpdateAndReload()
    }
    fun removeRecipe(id: String) {
        _recipes.value = _recipes.value.filter { it.id != id }
        sendUpdateAndReload()
    }
    fun setRecipePortions(id: String, portions: Int) {
        _recipes.value = _recipes.value.map {
            if (it.id == id) it.copy(portions = portions.coerceAtLeast(1).coerceAtMost(999)) else it
        }
        sendUpdateAndReload()
    }
}