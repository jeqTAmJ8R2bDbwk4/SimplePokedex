package com.example.pokedex.models

import com.example.pokedex.utils.NonEmpty
import com.example.pokedex.utils.validateNonEmpty

data class Description(
    @NonEmpty val gameVersionName: String,
    val localizedGameVersionName: String,
    @NonEmpty val description: String
) {
    init { validateNonEmpty() }
    fun getName() = localizedGameVersionName.ifEmpty(::gameVersionName)
}