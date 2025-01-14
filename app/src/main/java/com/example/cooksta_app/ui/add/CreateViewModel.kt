package com.example.cooksta_app.ui.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreateViewModel : ViewModel() {

    private val _portions = MutableLiveData(1)
    val portions: LiveData<Int> = _portions

    fun setPortions(value: Int) {
        _portions.value = value
    }
}