package com.example.cooksta_app.network

import com.example.cooksta_app.model.StateResponse
import com.example.cooksta_app.model.LoginRequest
import com.example.cooksta_app.model.PartialUser
import com.example.cooksta_app.model.TokenResponse
import com.example.cooksta_app.model.User
import com.example.cooksta_app.model.Recipe
import com.example.cooksta_app.model.RefreshTokenRequest
import com.example.cooksta_app.model.ShoppingListResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
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

    @GET("users/me")
    suspend fun getMe(): Response<User>
    @GET("users/{userId}/shopping-list")
    suspend fun getShoppingList(@Path("userId") userId: String): Response<ShoppingListResponse>
    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): Response<User>
    @PATCH("users/{userId}")
    suspend fun patchUser(@Path("userId") userId: String, @Body user: PartialUser): Response<User>

    @GET("recipes/{recipeId}")
    suspend fun getRecipe(@Path("recipeId") recipeId: String): Response<Recipe>
    @Streaming
    @GET("recipes/{recipeId}/media")
    suspend fun getRecipeMedia(@Path("recipeId") recipeId: String): Response<ResponseBody>
    @GET("recipes/feed")
    suspend fun getRecipeFeed(@Query("limit") limit: Int? = null, @Query("page") page: Int? = null): Response<List<Recipe>>
    @GET("recipes/search")
    suspend fun searchRecipes(@Query("q") query: String, @Query("limit") limit: Int? = null, @Query("page") page: Int? = null): Response<List<Recipe>>

    @POST("recipes")
    suspend fun postRecipe(): Response<Recipe>

    @POST("recipes/{recipeId}/like")
    suspend fun postRecipeLike(@Path("recipeId") recipeId: String): Response<StateResponse>
    @DELETE("recipes/{recipeId}/like")
    suspend fun deleteRecipeLike(@Path("recipeId") recipeId: String): Response<StateResponse>
    @POST("recipes/{recipeId}/save")
    suspend fun postRecipeSave(@Path("recipeId") recipeId: String): Response<StateResponse>
    @DELETE("recipes/{recipeId}/save")
    suspend fun deleteRecipeSave(@Path("recipeId") recipeId: String): Response<StateResponse>
}
