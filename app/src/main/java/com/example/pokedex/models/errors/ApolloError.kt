package com.example.pokedex.models.errors

import com.apollographql.apollo.exception.*
import com.example.pokedex.utils.HttpStatusCode

/** A fetch error occurred while interacting with the server.
 * The error can be due to different causes, such as network issues, data processing problems, or server responses.
 */
sealed interface ApolloError : Error {

    /** A generic error occurred with no additional context.
     * This is used when the system cannot provide more specific information. */
    data object GenericError : ApolloError

    /** A network-related error occurred.
     * This can be due to issues such as a closed socket, DNS problems, or the WebSocket connection failing. */
    data object NetworkError : ApolloError

    /** An error occurred with the server’s response or a failure to process a GraphQL operation.
     * This includes validation errors or failure to establish necessary operations, like subscriptions. */
    data object ServerError : ApolloError

    /** The application encountered an issue while handling or interpreting data.
     * This includes errors related to missing or malformed data, invalid JSON, or unexpected response structures. */
    data object DataError : ApolloError

    /** An HTTP-related error occurred, such as receiving a non-200 response code.
     * This represents failures specifically tied to HTTP requests. */
    data class HttpError(val code: HttpStatusCode) : ApolloError

    companion object {
        /** Maps Apollo exceptions to a unified error model for handling different types of fetch errors.
         * This reduces the complexity of handling Apollo-specific exceptions by grouping related errors together.
         */
        fun fromException(e: ApolloException): ApolloError {
            return when (e) {
                is ApolloNetworkException, is ApolloWebSocketClosedException -> NetworkError
                is SubscriptionOperationException, is SubscriptionConnectionException, is RouterError -> ServerError
                is ApolloHttpException -> HttpError(HttpStatusCode.fromCode(e.statusCode))
                is NoDataException, is JsonDataException, is JsonEncodingException,
                is com.apollographql.apollo.exception.NullOrMissingField -> DataError
                else -> GenericError
            }
        }
    }
}
