package com.example.pokedex.models

import com.example.pokedex.utils.NonEmpty
import com.example.pokedex.utils.validateNonEmpty

data class AbilityDescription(
    @NonEmpty val text: String,
    val versionGroup: List<GameVersion>,
    val versionGroupId: Int
) {
    init { validateNonEmpty() }
}