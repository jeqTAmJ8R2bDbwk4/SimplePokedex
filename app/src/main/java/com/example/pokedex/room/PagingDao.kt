package com.example.pokedex.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.pokedex.room.models.PagingEntry
import com.example.pokedex.room.models.Pokemon
import com.example.pokedex.room.models.Specy
import com.example.pokedex.room.models.Type
import com.example.pokedex.room.views.CompletePokemon

@Dao
abstract class PagingDao: PokemonDao() {
    @Upsert
    abstract suspend fun upsertPagingEntries(pagingEntries: List<PagingEntry>)

    @Query("SELECT MAX(last_modified_epoch_ms) FROM paging_entry")
    abstract suspend fun getPagingLastUpdated(): Long?

    @Query("SELECT `offset` FROM paging_entry WHERE pokemon_id = :pokemonId")
    abstract fun getOffsetByPokemonIdOrNull(pokemonId: Int): Int?

    @Query("""
        SELECT
            CP.*
        FROM
            paging_entry PE 
            JOIN complete_pokemon CP
            ON PE.pokemon_id = CP.pokemon_id
        ORDER BY PE.`offset`
        """)
    abstract fun getPagingSource(): PagingSource<Int, CompletePokemon>

    @Transaction
    open suspend fun updatePage(
        types: List<Type>,
        species: List<Specy>,
        pokemon: List<Pokemon>,
        pokemonIds: List<Int>,
        offset: Int
    ) {
        upsertCompletePokemon(types, species, pokemon)
        val lastUpdatedEpochMs = System.currentTimeMillis()
        upsertPagingEntries(
            pokemonIds.mapIndexed { index, pokemonId ->
                PagingEntry(
                    pokemonId=pokemonId,
                    offset = offset + index,
                    lastModifiedEpochMs = lastUpdatedEpochMs
                )
            }
        )
    }
}