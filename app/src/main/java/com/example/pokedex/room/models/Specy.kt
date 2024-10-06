package com.example.pokedex.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pokedex.models.Pokemon as AppPokemon

@Entity(tableName = "specy")
data class Specy(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "localized_name")
    val localizedName: String,
    @ColumnInfo(name = "national_pokedex_number")
    val nationalPokedexNumber: Int,
    @ColumnInfo(name = "evolved_from_specy_id")
    val evolvedFromSpecyId: Int?
) {
    companion object {
        fun fromAppPokemon(pokemon: AppPokemon) = Specy(
            id = pokemon.specyId,
            name = pokemon.specyName,
            localizedName = pokemon.specyLocalizedName,
            nationalPokedexNumber = pokemon.specyNationalPokedexNumber,
            evolvedFromSpecyId = pokemon.specyEvolvedFromSpecyId
        )
    }
}