package com.example.cooksta_app.model

data class User(
    val id: String,
    val username: String,
    val passwordHash: String,
    val biography: String,
    val favorites: List<String>,
    val shoppingList: List<ShoppingListItem>,
    val isPrivate: Boolean
)

data class ShoppingListItem(
    val recipeId: String,
    val servings: Int
)
