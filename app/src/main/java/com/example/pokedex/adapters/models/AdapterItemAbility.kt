package com.example.pokedex.adapters.models

import com.example.pokedex.models.Ability as AbilityContent

sealed interface AdapterItemAbility {
    data class Ability(val content: AbilityContent) : AdapterItemAbility
    data class PlaceHolder(val id: Int) : AdapterItemAbility
}