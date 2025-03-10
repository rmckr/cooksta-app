package com.example.cooksta_app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cooksta_app.model.Recipe
import com.example.cooksta_app.model.User
import com.example.cooksta_app.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _userRecipes = MutableStateFlow<List<Recipe>?>(null)
    val userRecipes: StateFlow<List<Recipe>?> = _userRecipes

    fun loadUser(userId: String) {
        viewModelScope.launch {
            val userResponse = RetrofitClient.apiService.getUser(userId)
            if (userResponse.isSuccessful) {
                _user.value = userResponse.body()
            }
        }
    }

    fun loadUserRecipes(userId: String) {
        viewModelScope.launch {
            val recipesResponse = RetrofitClient.apiService.getUserRecipes(userId)
            if (recipesResponse.isSuccessful) {
                _userRecipes.value = recipesResponse.body()
            }
        }
    }

    fun loadUserData(userId: String) {
        loadUser(userId)
        loadUserRecipes(userId)
    }
}
