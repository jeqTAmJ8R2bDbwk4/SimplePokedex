package com.example.pokedex.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.pokedex.room.models.FavouriteEntry
import com.example.pokedex.room.models.Pokemon
import com.example.pokedex.room.models.Specy
import com.example.pokedex.room.models.Type
import com.example.pokedex.room.views.CompletePokemon
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FavouriteDao : PokemonDao() {
    @Upsert
    abstract suspend fun upsertFavouriteEntries(favouriteEntries: List<FavouriteEntry>)

    @Query(
        """
        DELETE FROM
            favourite_entry
        WHERE
            pokemon_id NOT IN (:pokemonIds)
        """
    )
    abstract suspend fun deleteNonMatchingFavouriteEntries(pokemonIds: List<Int>)

    @Query(
        """
        SELECT
            CP.*
        FROM
            complete_pokemon CP
            JOIN favourite_entry FE
            ON CP.pokemon_id = FE.pokemon_id
        ORDER BY
            FE.position ASC
        """
    )
    abstract fun getFavouritePokemonFlow(): Flow<List<CompletePokemon>>

    @Transaction
    open suspend fun updateFavourites(
        types: List<Type>,
        species: List<Specy>,
        pokemon: List<Pokemon>,
        pokemonIds: List<Int>
    ) {
        upsertCompletePokemon(types, species, pokemon)
        deleteNonMatchingFavouriteEntries(pokemonIds)
        upsertFavouriteEntries(
            pokemonIds.mapIndexed { index, pokemonId ->
                FavouriteEntry(pokemonId = pokemonId, position = index)
            }
        )
    }
}