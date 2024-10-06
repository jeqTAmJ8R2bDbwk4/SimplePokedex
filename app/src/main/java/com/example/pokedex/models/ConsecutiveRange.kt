package com.example.pokedex.models

sealed interface ConsecutiveRange {
    val start: Int
    val size: Int

    data class Mutable(
        override var start: Int,
        override var size: Int
    ): ConsecutiveRange
}

