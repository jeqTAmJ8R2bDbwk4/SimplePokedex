package com.example.pokedex.viewmodels

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.repositories.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: Repository) : ViewModel() {
    private val _snackbarMessageFlow = MutableSharedFlow<Int>()
    val snackbarMessageFlow: SharedFlow<Int> = _snackbarMessageFlow

    fun setSnackbarMessage(@StringRes message: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _snackbarMessageFlow.emit(message)
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearHistory()
        }
    }

    fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearCache()
        }
    }
}