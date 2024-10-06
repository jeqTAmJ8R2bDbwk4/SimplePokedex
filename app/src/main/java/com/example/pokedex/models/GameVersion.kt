package com.example.pokedex.models

import com.example.pokedex.utils.NonEmpty
import com.example.pokedex.utils.validateNonEmpty

data class GameVersion (
    val id: Int,
    @NonEmpty val name: String,
    val localizedName: String
) {
    init { validateNonEmpty() }

    @JvmName(name = "getNameKotlin")
    fun getName() = localizedName.ifEmpty(::name)
}