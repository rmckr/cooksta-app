package com.example.cooksta_app.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class LoginRequest(
    val username: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)

data class StateResponse(
    val state: Boolean
)

data class ShoppingListItem(
    val id: String,
    val title: String,
    val portions: Int
)
@Parcelize
data class Ingredient(
    val name: String,
    val amount: Number?,
    var isChecked: Boolean = false
) : Parcelable

data class ShoppingListResponse(
    val shoppingList: List<ShoppingListItem>,
    val ingredients: List<Ingredient>
)