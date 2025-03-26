package com.example.cooksta_app.ui.shared.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cooksta_app.model.Recipe
import com.example.cooksta_app.model.User
import com.example.cooksta_app.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class FeedPostViewModel : ViewModel() {
    private val _likes = MutableStateFlow((50..500).random())
    val likes: StateFlow<Int> = _likes
    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked
    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe: StateFlow<Recipe?> = _recipe

    private val _creator = MutableStateFlow<User?>(null)
    val creator: StateFlow<User?> = _creator
    private val _me = MutableStateFlow<User?>(null)
    val me: StateFlow<User?> = _me

    init {
        viewModelScope.launch {
            combine(me, recipe) {
                    me, recipe -> me?.favorites?.contains(recipe?.id) == true
            }.collect { isSaved ->
                _isSaved.value = isSaved
            }
        }
    }

    fun toggleLike() {
        _isLiked.value = !_isLiked.value
        if (_isLiked.value) {
            _likes.value++
        } else {
            _likes.value--
        }

        // TODO: Not implemented in API
//        if (_recipe.value != null) {
//            viewModelScope.launch {
//                if (_isLiked.value) {
//                    RetrofitClient.apiService.postRecipeLike(_recipe.value!!.id)
//                } else {
//                    RetrofitClient.apiService.deleteRecipeLike(_recipe.value!!.id)
//                }
//            }
//        }
    }

    fun toggleSave() {
        _isSaved.value = !_isSaved.value

        if (_recipe.value != null) {
            viewModelScope.launch {
                if (_isSaved.value) {
                    RetrofitClient.apiService.postRecipeSave(_recipe.value!!.id)
                } else {
                    RetrofitClient.apiService.deleteRecipeSave(_recipe.value!!.id)
                }
            }
        }
    }

    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            val response = RetrofitClient.apiService.getRecipe(recipeId)
            if (response.isSuccessful) {
                _recipe.value = response.body()
            }
        }
    }

    fun loadCreator(userId: String) {
        viewModelScope.launch {
            val userResponse = RetrofitClient.apiService.getUser(userId)
            if (userResponse.isSuccessful) {
                _creator.value = userResponse.body()
            }
        }
    }

    fun loadMe() {
        viewModelScope.launch {
            val userResponse = RetrofitClient.apiService.getUser("me")
            if (userResponse.isSuccessful) {
                _me.value = userResponse.body()
            }
        }
    }
}