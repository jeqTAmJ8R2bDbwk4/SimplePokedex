package com.example.pokedex.models

import android.os.Parcelable
import com.example.pokedex.utils.NonEmpty
import com.example.pokedex.utils.validateNonEmpty
import kotlinx.parcelize.Parcelize
import com.example.pokedex.fragment.PokemonFragment as ApolloPokemon
import com.example.pokedex.room.views.CompletePokemon as RoomPokemon


@Parcelize
data class Pokemon(
    // Pokemon
    val id: Int,
    val spriteUrl: String,
    val officialSpriteUrl: String,

    // Form
    val formName: String,
    val formLocalizedName: String,

    // Types
    val primaryType: Type,
    val secondaryType: Type?,

    // Specy
    val specyId: Int,
    @NonEmpty val specyName: String,
    val specyLocalizedName: String,
    val specyNationalPokedexNumber: Int,
    val specyEvolvedFromSpecyId: Int?
) : Parcelable {
    init { validateNonEmpty() }

    fun getName() = formLocalizedName.ifEmpty {specyLocalizedName.ifEmpty { formName.ifEmpty { specyName } } }

    companion object {
        @Throws(NullPointerException::class, IllegalArgumentException::class)
        fun fromRoomPokemon(completePokemon: RoomPokemon) = Pokemon(
            id = completePokemon.pokemonId,
            spriteUrl = completePokemon.pokemonSpriteUrl,
            officialSpriteUrl = completePokemon.pokemonOfficialSpriteUrl,
            specyId = completePokemon.specyId,
            specyName = completePokemon.specyName,
            specyLocalizedName = completePokemon.specyLocalizedName,
            specyNationalPokedexNumber = completePokemon.specyNationalPokedexNumber,
            primaryType = Type(
                id = completePokemon.primaryTypeId,
                name = completePokemon.primaryTypeName,
                localizedName = completePokemon.primaryTypeLocalizedName
            ),
            secondaryType = completePokemon.secondaryTypeId?.let {
                Type(
                    id = completePokemon.secondaryTypeId,
                    name = completePokemon.secondaryTypeName!!,
                    localizedName = completePokemon.secondaryTypeLocalizedName!!
                )
            },
            formName = completePokemon.formName,
            formLocalizedName = completePokemon.formLocalizedName,
            specyEvolvedFromSpecyId = completePokemon.specyEvolvedFromSpecyId
        )


        @Throws(NullPointerException::class)
        fun fromApolloPokemon(pokemon: ApolloPokemon): Pokemon {
            val sprite = pokemon.sprites.firstOrNull()?.pokemonSpriteFragment
            val officialSprite = pokemon.officialSprites.firstOrNull()?.pokemonOfficialSpriteFragment
            val specy = pokemon.specy!!.pokemonSpecyFragment
            val pokemonTypes = pokemon.types.map { type -> type.pokemonTypesFragment.type!!.pokemonTypeFragment }
            val primaryType = pokemonTypes.first()
            val secondaryType = pokemonTypes.getOrNull(1)
            val form = pokemon.forms.firstOrNull()?.pokemonFormFragment

            return Pokemon(
                id = pokemon.id,
                spriteUrl = sprite?.sprites?.toString().orEmpty(),
                officialSpriteUrl = officialSprite?.sprites?.toString().orEmpty(),
                specyId = specy.id,
                specyName = specy.name,
                specyLocalizedName = specy.names.firstOrNull()?.name.orEmpty(),
                specyNationalPokedexNumber = specy.pokedex_numbers.first().pokedex_number,
                primaryType = Type(
                    id = primaryType.id,
                    name = primaryType.name,
                    localizedName = primaryType.names.firstOrNull()?.name.orEmpty()
                ),
                secondaryType = secondaryType?.let {
                    Type(
                        id = secondaryType.id,
                        name = secondaryType.name,
                        localizedName = secondaryType.names.firstOrNull()?.name.orEmpty()
                    )
                },
                formName = form?.name.orEmpty(),
                formLocalizedName = form?.names?.firstOrNull()?.name.orEmpty(),
                specyEvolvedFromSpecyId = specy.evolvesFromSpeciesId
            )
        }
    }
}