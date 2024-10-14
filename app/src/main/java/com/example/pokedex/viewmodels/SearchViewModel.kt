package com.example.pokedex.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.adapters.models.AdapterItemPokemon
import com.example.pokedex.adapters.models.AdapterItemPokemonMinimal
import com.example.pokedex.adapters.models.AdapterItemSearch
import com.example.pokedex.models.HistoryEntry
import com.example.pokedex.models.Pokemon
import com.example.pokedex.models.State
import com.example.pokedex.models.errors.RepositoryError
import com.example.pokedex.repositories.Repository
import com.example.pokedex.repositories.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(val repository: Repository): ViewModel() {
    companion object {
        private val pokemonPlaceholder = (0 .. 6).map(AdapterItemPokemon::Placeholder)
        private val popularPokemonPlaceholder = (0 .. 29).map(AdapterItemPokemonMinimal::Placeholder)

        private const val RECYCLER_VIEW = 0
        private const val EMPTY_QUERY_MESSAGE = 1
        private const val EMPTY_RESULT_MESSAGE = 2
        private const val ERROR_MESSAGE = 3
    }

    private sealed interface Helper {
        data object History: Helper
        data class Suggestion(val names: List<String>): Helper
    }

    private val _nameQuery = MutableStateFlow("")
    val nameQuery = _nameQuery
        .map { query -> query.replace("%", "").trim() }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val _pokemonQuery = MutableStateFlow("")
    val pokemonQuery = _pokemonQuery
        .map { query -> query.replace("%", "").trim() }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val popularPokemon = MutableStateFlow<List<AdapterItemPokemonMinimal>>(popularPokemonPlaceholder)
    private val history = repository.getHistoryFlow().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val helper = MutableStateFlow<Helper?>(null)
    val nameResults = combine(helper, popularPokemon, history) { helper, popularPokemon, history ->
        when (helper) {
            null -> emptyList()
            is Helper.History -> {
                buildList<AdapterItemSearch> {
                    addAll(history.map(AdapterItemSearch::HistoryEntry))
                    if (popularPokemon.isEmpty()) {
                        return@buildList
                    }
                    add(AdapterItemSearch.PopularTitle)
                    add(popularPokemon.let(AdapterItemSearch::Popular))
                }
            }
            is Helper.Suggestion -> {
                helper.names.map(AdapterItemSearch::Suggestion)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _error = MutableStateFlow<RepositoryError?>(null)
    val error = _error.asStateFlow()

    private val _pokemonResults = MutableStateFlow<List<AdapterItemPokemon>>(emptyList())
    val pokemonResults = _pokemonResults.asStateFlow()

    private val _state = MutableStateFlow(State.SUCCESS)
    val state = _state.asStateFlow()

    val displayedChild = combine(pokemonResults, pokemonQuery, state) { results, query, state ->
        return@combine when {
            state == State.ERROR -> ERROR_MESSAGE
            query.isEmpty() -> EMPTY_QUERY_MESSAGE
            results.isEmpty() -> EMPTY_RESULT_MESSAGE
            else -> RECYCLER_VIEW
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, RECYCLER_VIEW)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            nameQuery.debounce(500L).collect { query ->
                if (query.isEmpty()) {
                    helper.value = Helper.History
                    return@collect
                }
                val results = repository.getSuggestion(query)
                helper.value = when (results) {
                    is Result.Success -> results.data
                    is Result.Error -> emptyList()
                }.let(Helper::Suggestion)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getPopularPokemon()
            popularPokemon.value = when (result) {
                is Result.Error -> emptyList()
                is Result.Success -> result.data.map(AdapterItemPokemonMinimal::Pokemon)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            pokemonQuery.collect { query ->
                loadPokemonByQuery(query)
            }
        }
    }

    private suspend fun loadPokemonByQuery(query: String) {
        val searchResultCompareBy = compareBy<Pokemon>(
            { pokemon -> !pokemon.getName().startsWith(query) },
            { pokemon -> !pokemon.getName().startsWith(query, ignoreCase = true)},
            { pokemon -> !pokemon.getName().contains(query) },
            { pokemon -> !pokemon.getName().contains(query) }
        )

        if (query.isEmpty()) {
            _error.value = null
            _state.value = State.SUCCESS
            return
        }

        _state.value = State.LOADING
        _pokemonResults.value = pokemonPlaceholder

        repository.addHistoryEntry(HistoryEntry(query, LocalDateTime.now(ZoneOffset.UTC)))
        val result = repository.searchPokemon(query)
        when (result) {
            is Result.Success -> {
                _state.value = State.SUCCESS
                _error.value = null
                _pokemonResults.value = result.data.asSequence()
                    .sortedWith(searchResultCompareBy)
                    .map(AdapterItemPokemon::Pokemon)
                    .toList()
            }
            is Result.Error -> {
                _state.value = State.ERROR
                _error.value = result.error as RepositoryError
                _pokemonResults.value = emptyList()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val query = pokemonQuery.value
            loadPokemonByQuery(query)
        }
    }

    val favourites = repository
        .getFavouritePokemonFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val favouriteIdSet = favourites
        .map { favourites -> favourites.map(Pokemon::id).toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    fun searchNames(query: String) {
        _nameQuery.value = query
    }

    fun searchPokemon(query: String) {
        Timber.d("%s", query)
        _pokemonQuery.value = query

        /* When the user selects a Pokémon from their history, we want the names to be pre-loaded
         * for when they open the search window again. */
        _nameQuery.value = query
    }

    fun setIsFavourite(pokemon: Pokemon, isFavourite: Boolean) {
        var favourites = this.favourites.value.asSequence().filter { it.id != pokemon.id }
        if (isFavourite) {
            favourites += pokemon
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFavourites(favourites.toList())
        }
    }
}