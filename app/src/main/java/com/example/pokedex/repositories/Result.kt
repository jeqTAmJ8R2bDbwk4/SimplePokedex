package com.example.pokedex.repositories

import com.example.pokedex.models.errors.Error as ErrorContent

sealed interface Result<D, E: ErrorContent> {
    data class Success<D, E: ErrorContent>(val data: D): Result<D, E>
    data class Error<D, E: ErrorContent>(val error: ErrorContent): Result<D, E>
}