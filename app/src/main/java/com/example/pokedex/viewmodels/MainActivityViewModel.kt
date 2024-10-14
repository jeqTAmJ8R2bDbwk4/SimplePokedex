package com.example.pokedex.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MainActivityViewModel @Inject constructor(): ViewModel() {
    private var _initialized = false
    val initialized get() = _initialized

    fun initialize() {
        _initialized = true
    }
}