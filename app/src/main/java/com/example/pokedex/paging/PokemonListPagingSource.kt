package com.example.pokedex.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.example.pokedex.PokemonListPageQuery
import com.example.pokedex.models.Pokemon
import com.example.pokedex.models.Type
import com.example.pokedex.utils.RepositoryUtil
import com.example.pokedex.utils.squeeze
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.newFixedThreadPoolContext
import timber.log.Timber


class PokemonListPagingSource(
    private val apolloClient: ApolloClient
): PagingSource<Int, Pokemon>() {
    override fun getRefreshKey(state: PagingState<Int, Pokemon>): Int? {
        val closestPosition = state.anchorPosition?.let(state::closestPageToPosition)
        val prevKey = closestPosition?.prevKey
        val nextKey = closestPosition?.nextKey
        return RepositoryUtil.getRefreshKey(prevKey, nextKey, state.config.pageSize)
    }

    override val jumpingSupported: Boolean get() = true

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Pokemon> {
        val offset = params.key ?: 0
        val limit = params.loadSize
        return try {
            val response = apolloClient
                .query(
                    PokemonListPageQuery(
                        Optional.present(limit),
                        Optional.present(offset)
                    )
                )
                .execute()
            val exception = response.exception
            if (exception != null) {
                throw exception
            }
            val responseData = response.dataAssertNoErrors
            val count = responseData.count.aggregate!!.count
            val data = responseData.pokemon.map { pokemon ->
                Pokemon.fromApolloPokemon(pokemon.pokemonFragment)
            }
            RepositoryUtil.getLoadResult(count, offset, data)
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            Timber.e(e)
            LoadResult.Error(e)
        }
    }
}