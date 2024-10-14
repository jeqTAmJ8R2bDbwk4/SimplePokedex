package com.example.pokedex.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.pokedex.room.models.Pokemon
import com.example.pokedex.room.models.Specy
import com.example.pokedex.room.models.Type

// Using inheretance just to share upserting pokemon capabilities with other Dao's.
@Dao
abstract class PokemonDao() {
    @Upsert
    abstract suspend fun upsertPokemon(pokemon: List<Pokemon>)

    @Upsert
    abstract suspend fun upsertTypes(types: List<Type>)

    @Upsert
    abstract suspend fun upsertSpecies(species: List<Specy>)

    @Transaction
    open suspend fun upsertCompletePokemon(
        types: List<Type>,
        species: List<Specy>,
        pokemon: List<Pokemon>
    ) {
        upsertTypes(types)
        upsertSpecies(species)
        upsertPokemon(pokemon)
    }

    @Query("DELETE FROM history_entry;")
    abstract suspend fun clearHistoryEntires()

    @Query("DELETE FROM paging_entry;")
    abstract suspend fun clearPagingEntries()

    @Transaction
    open suspend fun clearCache() {
        clearHistoryEntires()
        clearPagingEntries()
    }
}