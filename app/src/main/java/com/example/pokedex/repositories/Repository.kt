package com.example.pokedex.repositories

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.apollographql.apollo.ApolloClient
import com.example.pokedex.models.HistoryEntry
import com.example.pokedex.models.Pokemon
import com.example.pokedex.paging.PokemonListRemoteMediator
import com.example.pokedex.room.PagingDao
import com.example.pokedex.room.PokemonDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val localRepository: LocalRepository,
    private val remoteRepository: RemoteRepository,
    private val apolloClient: ApolloClient,
    private val pokemonDao: PokemonDao,
    private val pagingDao: PagingDao
) {
    suspend fun getPokemonDetails(id: Int) = remoteRepository.getPokemonDetails(id)

    fun getFavouritePokemonFlow() = localRepository.getFavouritePokemonFlow()

    suspend fun updateFavourites(pokemon: List<Pokemon>) = localRepository.updateFavourites(pokemon)

    fun getHistoryFlow() = localRepository.getHistoryFlow()

    suspend fun getPopularPokemon() = remoteRepository.getPopularPokemon()

    suspend fun addHistoryEntry(historyEntry: HistoryEntry) {
        localRepository.addHistoryEntry(historyEntry)
    }

    suspend fun getSuggestion(query: String) = remoteRepository.getSuggestion(query)

    suspend fun searchPokemon(query: String) = remoteRepository.searchPokemon(query)

    suspend fun clearHistory() = localRepository.clearHistory()

    suspend fun clearCache() {
        localRepository.clearCache()
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getPagedPokemon() = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = true),
        remoteMediator = PokemonListRemoteMediator(apolloClient, pokemonDao, pagingDao),
        pagingSourceFactory = { localRepository.getPagingSource() }
    )
}