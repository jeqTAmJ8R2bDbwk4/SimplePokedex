package com.example.pokedex.adapters.models

import com.example.pokedex.models.Pokemon as PokemonContent

sealed interface AdapterItemPokemon {
    data class Pokemon(val content: PokemonContent) : AdapterItemPokemon
    data class Placeholder(val id: Int) : AdapterItemPokemon
}