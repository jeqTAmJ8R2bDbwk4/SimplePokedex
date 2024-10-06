package com.example.pokedex.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.pokedex.room.models.HistoryEntry
import com.example.pokedex.room.models.ListEntry
import com.example.pokedex.room.models.Pokemon
import com.example.pokedex.room.models.Specy
import com.example.pokedex.room.models.Type
import com.example.pokedex.room.views.CompletePokemon
import com.example.pokedex.utils.ALL_LIST_ID
import com.example.pokedex.utils.FAVOURITE_LIST_ID
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    @Upsert
    suspend fun upsertPokemon(pokemon: List<Pokemon>)

    @Upsert
    suspend fun upsertTypes(types: List<Type>)

    @Upsert
    suspend fun upsertSpecies(species: List<Specy>)

    @Upsert
    suspend fun usertListEntry(listEntries: List<ListEntry>)

    @Upsert
    suspend fun upsertHistoryEntry(historyEntry: HistoryEntry)

    @Query("DELETE FROM list_entry WHERE pokemon_id NOT IN (:pokemonIds) AND list_id = $FAVOURITE_LIST_ID")
    suspend fun deleteFavouritesIfIdNotIn(pokemonIds: List<Int>)

    @Transaction
    suspend fun updateFavourites(pokemonIds: List<Int>) {
        deleteFavouritesIfIdNotIn(pokemonIds)
        usertListEntry(
            pokemonIds.mapIndexed { index, pokemonId ->
                ListEntry(pokemonId=pokemonId, position = index, listId = FAVOURITE_LIST_ID)
            }
        )
    }

    suspend fun updatePage(pokemonIds: List<Int>, startPos: Int) {
        usertListEntry(
            pokemonIds.mapIndexed { index, pokemonId ->
                ListEntry(pokemonId=pokemonId, position = startPos + index, listId = ALL_LIST_ID)
            }
        )
    }

    @Query("SELECT CP.* FROM complete_pokemon CP JOIN list_entry LE ON CP.pokemon_id = LE.pokemon_id AND list_id = $FAVOURITE_LIST_ID ORDER BY LE.position ASC")
    fun getFavouritePokemonFlow(): Flow<List<CompletePokemon>>

    @Query("SELECT * FROM history_entry HE ORDER BY HE.timestamp_ms DESC")
    suspend fun getHistory(): List<HistoryEntry>
}