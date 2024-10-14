package com.example.pokedex.viewmodels

import androidx.lifecycle.ViewModel
import com.example.pokedex.repositories.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val repository: Repository
): ViewModel() {
    private var _initialized = false
    val inialized get() = _initialized

    fun initialize() {
        _initialized = true
    }
}