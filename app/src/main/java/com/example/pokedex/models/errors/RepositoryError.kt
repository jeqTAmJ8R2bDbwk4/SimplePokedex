package com.example.pokedex.models.errors

import com.example.pokedex.models.errors.ApolloError as ApolloExceptionErrorContent

sealed interface RepositoryError: Error {
    data object DataMappingException : RepositoryError
    data class ApolloError(val content: ApolloExceptionErrorContent) : RepositoryError
}