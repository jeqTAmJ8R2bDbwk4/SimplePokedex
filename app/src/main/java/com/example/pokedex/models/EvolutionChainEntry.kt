package com.example.pokedex.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EvolutionChainEntry(
    val id: Int,
    val content: Pokemon
): Parcelable