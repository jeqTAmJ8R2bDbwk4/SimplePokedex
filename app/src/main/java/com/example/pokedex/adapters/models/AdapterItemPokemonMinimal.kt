package com.example.pokedex.adapters.models
import com.example.pokedex.models.PokemonMinimal as PokemonContent

sealed interface AdapterItemPokemonMinimal {
    data class Pokemon(val content: PokemonContent): AdapterItemPokemonMinimal
    data class Placeholder(val id: Int): AdapterItemPokemonMinimal
}