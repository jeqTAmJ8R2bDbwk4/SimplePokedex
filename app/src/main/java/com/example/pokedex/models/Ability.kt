package com.example.pokedex.models

import com.example.pokedex.utils.NonEmpty
import com.example.pokedex.utils.validateNonEmpty

data class Ability(
    val id: Int,
    @NonEmpty val name: String,
    val localizedName: String,
    val isHidden: Boolean,
    val descriptions: List<AbilityDescription>,
) {
    init { validateNonEmpty() }
    @JvmName(name = "getNameKotlin")
    fun getName() = localizedName.ifEmpty(::name)
}
