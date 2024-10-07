package com.example.pokedex.adapters.models

import com.example.pokedex.models.HistoryEntry as HistoryEntryContent

sealed interface AdapterItemSearch {
    data class HistoryEntry(val content: HistoryEntryContent): AdapterItemSearch
    data class Suggestion(val name: String): AdapterItemSearch
    data object PopularTitle: AdapterItemSearch
    data class Popular(val content: List<AdapterItemPokemon>): AdapterItemSearch
}