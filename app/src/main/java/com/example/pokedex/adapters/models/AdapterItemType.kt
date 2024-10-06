package com.example.pokedex.adapters.models

sealed interface AdapterItemType {
    data class Type(val id: Int): AdapterItemType
    data class Placeholder(val id: Int): AdapterItemType
    sealed interface Multiplier: AdapterItemType
    object Quater: Multiplier
    object Half: Multiplier
    object Double: Multiplier
    object Quadruple: Multiplier
    object Immune: Multiplier
}