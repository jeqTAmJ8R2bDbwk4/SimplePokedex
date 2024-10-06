package com.example.pokedex.repositories

import com.example.pokedex.models.Pokemon
import com.example.pokedex.models.HistoryEntry as AppHistoryEntry
import com.example.pokedex.room.models.HistoryEntry as RoomHistoryEntry
import com.example.pokedex.room.models.Pokemon as RoomPokemon
import com.example.pokedex.models.Pokemon as AppPokemon
import com.example.pokedex.room.models.Type as RoomType
import com.example.pokedex.room.Dao
import kotlinx.coroutines.flow.distinctUntilChanged
import com.example.pokedex.room.models.Specy as RoomSpecy
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class LocalRepository @Inject constructor(val dao: Dao) {
    fun getFavouritePokemonFlow() = dao
        .getFavouritePokemonFlow()
        .distinctUntilChanged()
        .map { pokemon ->
            pokemon.map(AppPokemon::fromRoomPokemon)
        }

    suspend fun updatePokemon(pokemon: List<Pokemon>) {
        val types = (pokemon.asSequence().map(AppPokemon::primaryType) + pokemon.asSequence().mapNotNull(AppPokemon::secondaryType)).toList()
        dao.upsertTypes(types.map(RoomType::fromAppType))
        dao.upsertSpecies(pokemon.asSequence().map(RoomSpecy::fromAppPokemon).toList())
        dao.upsertPokemon(pokemon.map(RoomPokemon::fromAppPokemon))
    }

    suspend fun updateFavourites(pokemon: List<AppPokemon>) {
        updatePokemon(pokemon)
        dao.updateFavourites(pokemon.map(AppPokemon::id))
    }

    suspend fun updatePage(pokemon: List<Pokemon>, startPos: Int) {
        updatePokemon(pokemon)
        dao.updatePage(pokemon.map(AppPokemon::id), startPos)
    }

    suspend fun getHistory(): List<AppHistoryEntry> {
        return dao.getHistory().map(AppHistoryEntry::fromRoomHistoryEntry)
    }

    suspend fun addHistoryEntry(historyEntry: AppHistoryEntry) {
        return dao.upsertHistoryEntry(RoomHistoryEntry.fromAppHistoryEntry(historyEntry))
    }
}