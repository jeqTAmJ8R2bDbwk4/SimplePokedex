package com.example.pokedex.adapters.models

import com.example.pokedex.models.EvolutionChainEntry
import com.example.pokedex.models.Pokemon

sealed interface AdapterItemEvolutionChainEdge {
    data class EvolutionChainEdge(
        val base: EvolutionChainEntry,
        val evolution: EvolutionChainEntry
    ): AdapterItemEvolutionChainEdge
    data class Placeholder(val id: Int): AdapterItemEvolutionChainEdge
}