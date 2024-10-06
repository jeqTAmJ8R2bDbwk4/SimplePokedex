package com.example.pokedex.utils

import androidx.core.graphics.Insets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainActivityInfo @Inject constructor() {
    private val _bottomNavigationBarHeightPx = MutableStateFlow<Insets>(Insets.of(0, 0, 0, 0))
    val bottomNavigationBarInsets = _bottomNavigationBarHeightPx.asStateFlow()

    fun setBottomNavigationBarHeightPx(heightPx: Int) {
        _bottomNavigationBarHeightPx.value = Insets.of(0, 0, 0, heightPx)
    }
}