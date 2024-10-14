package com.example.pokedex.models.errors

import com.apollographql.apollo.exception.ApolloException
import com.example.pokedex.models.errors.ApolloError as ApolloExceptionErrorContent

sealed interface RepositoryError: Error {
    companion object {
        fun fromException(exception: Exception): RepositoryError {
            return when(exception) {
                is ApolloException -> {
                    val error = ApolloExceptionErrorContent.fromException(exception)
                    ApolloError(error)
                }
                else -> {
                    DataMappingException
                }
            }
        }
    }

    data object DataMappingException : RepositoryError
    data class ApolloError(val content: ApolloExceptionErrorContent) : RepositoryError
}