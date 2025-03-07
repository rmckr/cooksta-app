package com.example.cooksta_app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cooksta_app.model.Recipe
import com.example.cooksta_app.network.RetrofitClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val _results = MutableSharedFlow<List<Recipe>>(replay = 1)
    val results: SharedFlow<List<Recipe>> get() = _results

    fun search(query: String) {
        viewModelScope.launch {
            val searchResponse = RetrofitClient.apiService.searchRecipes(query)
            if (searchResponse.isSuccessful) {
                _results.emit(searchResponse.body() ?: emptyList())
            } else {
                _results.emit(emptyList())
            }
        }
    }
}