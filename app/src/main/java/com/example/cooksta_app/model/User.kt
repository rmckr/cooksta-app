package com.example.cooksta_app.model

data class User(
    val id: String,
    val username: String,
    val password: String,
    val biography: String,
    val favorites: List<String>,
    val shoppingList: Map<String, Int>,
    val isPrivate: Boolean
)
