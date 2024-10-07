package com.example.pokedex.repositories

import com.example.pokedex.models.HistoryEntry
import com.example.pokedex.models.Pokemon
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val localRepository: LocalRepository,
    private val remoteRepository: RemoteRepository
) {
    fun getAllPokemon() = remoteRepository.getAllPokemon()

    suspend fun getPokemonDetails(id: Int) = remoteRepository.getPokemonDetails(id)

    fun getFavouritePokemonFlow() = localRepository.getFavouritePokemonFlow().onEach { Timber.d("getFavouritePokemonFlow: %s", it.joinToString(", "))}

    suspend fun updateFavourites(pokemon: List<Pokemon>) = localRepository.updateFavourites(pokemon)

    suspend fun getHistory() = localRepository.getHistory()

    suspend fun getPopularPokemon() = remoteRepository.getPopularPokemon()

    suspend fun addHistoryEntry(historyEntry: HistoryEntry) {
        localRepository.addHistoryEntry(historyEntry)
    }

    suspend fun getSuggestion(query: String) = remoteRepository.getSuggestion(query)

    suspend fun searchPokemon(query: String) = remoteRepository.searchPokemon(query)
}