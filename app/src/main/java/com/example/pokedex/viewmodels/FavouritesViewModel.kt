package com.example.pokedex.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.models.Pokemon
import com.example.pokedex.repositories.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Collections
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(private val repository: Repository): ViewModel() {
    companion object {
        private const val RECYCLER_VIEW = 0
        private const val EMPTY_MESSAGE = 1
    }

    private val _favourites = repository.getFavouritePokemonFlow()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            initialValue = null
        )
    val favourites = _favourites.filterNotNull()

    private val _displayedChild = MutableStateFlow(RECYCLER_VIEW)
    val displayedChild = _displayedChild.asStateFlow()

    fun setIsEmpty(isEmpty: Boolean) {
        _displayedChild.value = if (isEmpty) {
            EMPTY_MESSAGE
        } else {
            RECYCLER_VIEW
        }
    }

    fun removeFavourite(pokemon: Pokemon) {
        var favourites = _favourites.value ?: return
        favourites = favourites.filter { it.id != pokemon.id }
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFavourites(favourites)
        }
    }

    fun moveFavourite(srcPos: Int, dstPos: Int) {
        var favourites = _favourites.value?.toMutableList() ?: return
        Timber.d("Before swap: %s", favourites.map(Pokemon::id).joinToString(", "))
        Collections.swap(favourites, srcPos, dstPos)
        Timber.d("After swap: %s", favourites.map(Pokemon::id).joinToString(", "))
        Timber.d("Moved $srcPos $dstPos favourite: $favourites")
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFavourites(favourites)
        }
    }

    fun swipeFavourite(position: Int) {
        var favourites = _favourites.value ?: return
        favourites = favourites.filterIndexed { index, _ -> index != position }
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFavourites(favourites)
        }
    }
}