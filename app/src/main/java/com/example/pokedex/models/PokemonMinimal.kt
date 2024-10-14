package com.example.pokedex.models

import android.os.Parcelable
import com.example.pokedex.utils.NonEmpty
import com.example.pokedex.utils.validateNonEmpty
import kotlinx.parcelize.Parcelize
import com.example.pokedex.fragment.PokemonMinimalFragment as ApolloPokemon


@Parcelize
data class PokemonMinimal(
    // Pokemon
    val id: Int,
    val spriteUrl: String,

    // Form
    val formName: String,
    val formLocalizedName: String,

    // Specy
    @NonEmpty val specyName: String,
    val specyLocalizedName: String,
) : Parcelable {
    init { validateNonEmpty() }

    fun getName() = formLocalizedName.ifEmpty {
        specyLocalizedName.ifEmpty {
            formName.ifEmpty {
                specyName
            }
        }
    }

    companion object {
        @Throws(NullPointerException::class)
        fun fromApolloPokemon(pokemon: ApolloPokemon): PokemonMinimal {
            val sprite = pokemon.sprites.firstOrNull()?.pokemonSpriteFragment
            val specy = pokemon.specy!!.pokemonSpecyMinimalFragment
            val form = pokemon.forms.firstOrNull()?.pokemonFormFragment

            return PokemonMinimal(
                id = pokemon.id,
                spriteUrl = sprite?.sprites?.toString().orEmpty(),
                specyName = specy.name,
                specyLocalizedName = specy.names.firstOrNull()?.name.orEmpty(),
                formName = form?.name.orEmpty(),
                formLocalizedName = form?.names?.firstOrNull()?.name.orEmpty()
            )
        }
    }
}