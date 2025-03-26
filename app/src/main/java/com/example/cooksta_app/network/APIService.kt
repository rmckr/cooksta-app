package com.example.cooksta_app.network

import com.example.cooksta_app.model.Ingredient
import com.example.cooksta_app.model.LoginRequest
import com.example.cooksta_app.model.PartialRecipe
import com.example.cooksta_app.model.PartialUser
import com.example.cooksta_app.model.Recipe
import com.example.cooksta_app.model.RecipeBody
import com.example.cooksta_app.model.RefreshTokenRequest
import com.example.cooksta_app.model.ShoppingListItem
import com.example.cooksta_app.model.ShoppingListItemBody
import com.example.cooksta_app.model.TokenResponse
import com.example.cooksta_app.model.User
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface APIService {
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>
    @POST("/logout")
    suspend fun logout(@Body request: RefreshTokenRequest): Response<Any>
    @POST("/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequest): Response<TokenResponse>

    @Multipart
    @PUT("users/me/avatar")
    suspend fun putAvatar(@Part avatar: MultipartBody.Part): Response<Void>

    @GET("users/me/shopping-list")
    suspend fun getShoppingList(): Response<List<ShoppingListItem>>
    @POST("users/me/shopping-list/items")
    suspend fun postShoppingListItem(@Body item: ShoppingListItemBody): Response<List<ShoppingListItem>>
    @DELETE("users/me/shopping-list/items/{recipeId}")
    suspend fun deleteShoppingListItem(@Path("recipeId") recipeId: String): Response<List<ShoppingListItem>>
    @DELETE("users/me/shopping-list/items")
    suspend fun deleteShoppingList(): Response<Void>
    @GET("users/me/shopping-list/ingredients")
    suspend fun getShoppingListIngredients(): Response<List<Ingredient>>

    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): Response<User>
    @PATCH("users/{userId}")
    suspend fun patchUser(@Path("userId") userId: String, @Body user: PartialUser): Response<User>
    @GET("users/{userId}/recipes")
    suspend fun getUserRecipes(@Path("userId") userId: String): Response<List<Recipe>>

    @GET("recipes/search")
    suspend fun searchRecipes(@Query("q") query: String, @Query("limit") limit: Int? = null, @Query("page") page: Int? = null): Response<List<Recipe>>
    @Streaming
    @GET("recipes/{recipeId}/media")
    suspend fun getRecipeMedia(@Path("recipeId") recipeId: String): Response<ResponseBody>
    @Multipart
    @PUT("recipes/{recipeId}/media")
    suspend fun putRecipeMedia(@Path("recipeId") recipeId: String, @Part media: MultipartBody.Part): Response<Void>
    @GET("recipes/feed")
    suspend fun getRecipeFeed(@Query("limit") limit: Int? = null, @Query("page") page: Int? = null): Response<List<Recipe>>

    @POST("recipes")
    suspend fun postRecipe(@Body recipe: RecipeBody): Response<Recipe>
    @GET("recipes/{recipeId}")
    suspend fun getRecipe(@Path("recipeId") recipeId: String): Response<Recipe>
    @PATCH("recipes/{recipeId}")
    suspend fun patchRecipe(@Path("recipeId") recipeId: String, @Body recipe: PartialRecipe): Response<Recipe>
    @DELETE("recipes/{recipeId}")
    suspend fun deleteRecipe(@Path("recipeId") recipeId: String): Response<Void>
}
