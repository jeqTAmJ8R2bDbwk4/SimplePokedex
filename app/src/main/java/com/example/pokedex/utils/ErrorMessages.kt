package com.example.pokedex.utils

import androidx.annotation.StringRes
import com.example.pokedex.R
import com.example.pokedex.models.errors.ApolloError
import com.example.pokedex.models.errors.RepositoryError

@StringRes
fun errorToMessageResource(error: ApolloError): Int {
   return  when(error) {
        ApolloError.DataError -> R.string.error_message_data
        ApolloError.ServerError -> R.string.error_message_server
        ApolloError.NetworkError -> R.string.error_message_network
        ApolloError.GenericError -> R.string.error_message_generic
        is ApolloError.HttpError -> when(error.code) {
            HttpStatusCode.BadRequest -> R.string.error_message_http_400_bad_request
            HttpStatusCode.Forbidden -> R.string.error_message_http_403_forbidden
            HttpStatusCode.NotFound -> R.string.error_message_http_404_not_found
            HttpStatusCode.RequestTimeout -> R.string.error_message_http_408_request_timeout
            HttpStatusCode.TooManyRequests -> R.string.error_message_http_429_too_many_requests
            HttpStatusCode.InternalServerError -> R.string.error_message_http_500_internal_server_error
            HttpStatusCode.ServiceUnavailable -> R.string.error_message_http_503_service_unavailable
            else -> R.string.error_message_generic
        }
    }
}

@StringRes
fun errorToMessageResource(error: RepositoryError): Int {
    return when(error) {
        RepositoryError.DataMappingException -> R.string.error_message_data_mapping
        is RepositoryError.ApolloError -> errorToMessageResource(error.content)
    }
}