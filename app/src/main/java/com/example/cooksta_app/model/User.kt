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

data class PartialUser(
    val id: String? = null,
    val username: String? = null,
    val password: String? = null,
    val biography: String? = null,
    val favorites: List<String>? = null,
    val shoppingList: Map<String, Int>? = null,
    val isPrivate: Boolean? = null
)
