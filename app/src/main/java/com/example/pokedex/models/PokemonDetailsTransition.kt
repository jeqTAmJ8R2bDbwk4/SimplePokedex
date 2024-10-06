package com.example.pokedex.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PokemonDetailsTransition(
    val transitionName: String,
    val pokemon: Pokemon
) : Parcelable