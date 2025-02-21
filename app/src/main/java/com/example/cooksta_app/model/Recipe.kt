package com.example.cooksta_app.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Recipe(
    @Suppress("PropertyName") val _id: String,
    val creatorId: String,
    val title: String,
    val description: String,
    val ingredients: Map<String, Number?>,
    val portions: Int,
    val categories: List<Category>,
    val isPrivate: Boolean
) : Parcelable

enum class Category {
    @SerializedName("asian") ASIAN,
    @SerializedName("baking") BAKING,
    @SerializedName("dessert") DESSERT,
    @SerializedName("drink") DRINK,
    @SerializedName("main") MAIN,
    @SerializedName("italian") ITALIAN,
    @SerializedName("quick") QUICK,
    @SerializedName("soup") SOUP,
    @SerializedName("vegetarian") VEGETARIAN
}