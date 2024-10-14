package com.example.pokedex.repositories

import android.database.sqlite.SQLiteConstraintException
import com.example.pokedex.models.HistoryEntry
import com.example.pokedex.models.Pokemon
import com.example.pokedex.room.Database
import com.example.pokedex.room.FavouriteDao
import com.example.pokedex.room.HistoryDao
import com.example.pokedex.room.PagingDao
import com.example.pokedex.room.PokemonDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import com.example.pokedex.room.models.HistoryEntry as RoomHistoryEntry
import com.example.pokedex.room.models.Pokemon as RoomPokemon
import com.example.pokedex.room.models.Specy as RoomSpecy
import com.example.pokedex.room.models.Type as RoomType


@Singleton
class LocalRepository @Inject constructor(
    private val database: Database,
    private val pokemonDao: PokemonDao,
    private val historyDao: HistoryDao,
    private val favouriteDao: FavouriteDao,
    private val pagingDao: PagingDao
) {
    companion object {
        private fun pokemonToRoom(
            pokemon: List<Pokemon>
        ): Triple<List<RoomType>, List<RoomSpecy>, List<RoomPokemon>> {
            val primaryRoomTypes = pokemon.asSequence()
                .map(Pokemon::primaryType)
                .map(RoomType::fromAppType)
            val secondaryRoomTypes = pokemon.asSequence()
                .mapNotNull(Pokemon::secondaryType)
                .map(RoomType::fromAppType)
            val roomTypes = (primaryRoomTypes + secondaryRoomTypes).toList()
            val roomSpecies = pokemon.map(RoomSpecy::fromAppPokemon)
            val roomPokemon = pokemon.map(RoomPokemon::fromAppPokemon)
            return Triple(roomTypes, roomSpecies, roomPokemon)
        }
    }

    fun getFavouritePokemonFlow() = favouriteDao
        .getFavouritePokemonFlow()
        .distinctUntilChanged()
        .map { pokemon ->
            pokemon.map(Pokemon::fromRoomPokemon)
        }

    suspend fun updateFavourites(pokemon: List<Pokemon>) {
        val roomPokemonIds = pokemon.map(Pokemon::id)
        val (roomType, roomSpecy, roomPokemon) = pokemonToRoom(pokemon)
        favouriteDao.updateFavourites(roomType, roomSpecy, roomPokemon, roomPokemonIds)
    }

    fun getHistoryFlow(): Flow<List<HistoryEntry>> {
        return historyDao.getHistoryFlow().distinctUntilChanged().map { history ->
            history.map(HistoryEntry::fromRoomHistoryEntry)
        }
    }

    suspend fun addHistoryEntry(historyEntry: HistoryEntry) {
        return historyDao.upsertHistoryEntry(RoomHistoryEntry.fromAppHistoryEntry(historyEntry))
    }

    suspend fun clearHistory() {
        pokemonDao.clearHistoryEntires()
    }

    suspend fun clearCache() {
        try {
            pokemonDao.clearCache()
        } catch (e: SQLiteConstraintException) {
            Timber.e(e)
        }
    }

    fun getPagingSource() = pagingDao.getPagingSource()
}