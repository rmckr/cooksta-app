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

    sealed class UserState {
        object Loading : UserState()
        data class Success(val data: User) : UserState()
        data class Error(val message: String) : UserState()
    }

    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState: StateFlow<UserState> = _userState

    sealed class UserRecipesState {
        object Loading : UserRecipesState()
        data class Success(val data: List<Recipe>) : UserRecipesState()
        data class Error(val message: String) : UserRecipesState()
    }

    private val _userRecipesState = MutableStateFlow<UserRecipesState>(UserRecipesState.Loading)
    val userRecipesState: StateFlow<UserRecipesState> = _userRecipesState

    fun loadUser(userId: String) {
        _userState.value = UserState.Loading
        viewModelScope.launch {
            val userResponse = RetrofitClient.apiService.getUser(userId)
            if (userResponse.isSuccessful) {
                _userState.value = UserState.Success(userResponse.body()!!)
            } else {
                _userState.value = UserState.Error("Fehler beim Laden des Benutzers")
            }
        }
    }

    fun loadUserRecipes(userId: String) {
        _userRecipesState.value = UserRecipesState.Loading
        viewModelScope.launch {
            val recipesResponse = RetrofitClient.apiService.getUserRecipes(userId)
            if (recipesResponse.isSuccessful) {
                _userRecipesState.value = UserRecipesState.Success(recipesResponse.body()!!)
            } else {
                _userRecipesState.value = UserRecipesState.Error("Fehler beim Laden der Rezepte")
            }
        }
    }

    fun loadUserData(userId: String) {
        loadUser(userId)
        loadUserRecipes(userId)
    }
}
