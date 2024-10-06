package com.example.pokedex.viewmodels

import androidx.lifecycle.ViewModel
import com.example.pokedex.repositories.RemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val remoteRepository: RemoteRepository
): ViewModel() {
    private var initializedSearch = false

}