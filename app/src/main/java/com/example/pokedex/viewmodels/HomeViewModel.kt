package com.example.pokedex.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.pokedex.adapters.models.AdapterItemSearch
import com.example.pokedex.adapters.models.AdapterItemType
import com.example.pokedex.models.Pokemon
import com.example.pokedex.models.State
import com.example.pokedex.models.errors.RepositoryError
import com.example.pokedex.repositories.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    companion object {
        private const val RECYCLER_VIEW = 0
        private const val ERROR_MESSAGE = 1
    }

    val favourites = repository
        .getFavouritePokemonFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _weeknessFourth = MutableStateFlow((0 until 6).map(AdapterItemType::Placeholder))
    val weeknessFourth = _weeknessFourth.asStateFlow()
    private val _weeknessHalf = MutableStateFlow((0 until 6).map(AdapterItemType::Placeholder))
    val weeknessHelf = _weeknessHalf.asStateFlow()
    private val _weeknessDouble = MutableStateFlow((0 until  6).map(AdapterItemType::Placeholder))
    val weeknessDouble = _weeknessDouble.asStateFlow()
    private val _weeknessQuadruple = MutableStateFlow((0 until 6).map(AdapterItemType::Placeholder))
    val weeknessQuadruple = _weeknessQuadruple.asStateFlow()

    private val _isFABVisible = MutableStateFlow(false)
    val isFABVisible: StateFlow<Boolean> = _isFABVisible

    private val _state = MutableStateFlow(State.SUCCESS)
    val state = _state.asStateFlow()

    val displayedChild = state.map { state ->
        return@map when(state) {
            State.SUCCESS -> RECYCLER_VIEW
            State.ERROR -> ERROR_MESSAGE
            State.LOADING -> RECYCLER_VIEW
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, RECYCLER_VIEW)

    private val _error = MutableStateFlow<RepositoryError?>(null)
    val error = _error.asStateFlow()

    private val _snackbarError = MutableSharedFlow<RepositoryError?>()
    val snackbarError = _snackbarError.asSharedFlow()

    private val _searchResults = MutableStateFlow(emptyList<AdapterItemSearch>())
    val searchResults = _searchResults.asStateFlow()

    val pagingFlow = repository.getPagedPokemon()
        .flow.flowOn(Dispatchers.IO)
        .map { pokemon -> pokemon.map(Pokemon::fromRoomPokemon) }
        .cachedIn(viewModelScope)

    fun setIsFavourite(pokemon: Pokemon, isFavourite: Boolean) {
        var favourites = this.favourites.value.asSequence().filter { it.id != pokemon.id }
        if (isFavourite) {
            favourites += pokemon
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFavourites(favourites.toList())
        }
    }

    fun setFABVisibility(show: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _isFABVisible.emit(show)
        }
    }

    fun setRefreshState(state: State) {
        _state.value = state
    }

    fun showSnackbarError(error: RepositoryError?) {
        viewModelScope.launch(Dispatchers.IO) {
            _snackbarError.emit(error)
        }
    }

    fun setRefreshError(error: RepositoryError?) {
        _error.value = error
    }
}