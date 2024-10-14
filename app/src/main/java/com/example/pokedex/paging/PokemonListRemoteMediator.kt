package com.example.pokedex.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.apollographql.apollo.ApolloClient
import com.example.pokedex.PokemonListPageQuery
import com.example.pokedex.models.Pokemon
import com.example.pokedex.room.PagingDao
import com.example.pokedex.room.PokemonDao
import com.example.pokedex.room.views.CompletePokemon
import timber.log.Timber
import kotlin.time.Duration.Companion.minutes
import com.example.pokedex.room.models.Pokemon as RoomPokemon
import com.example.pokedex.room.models.Specy as RoomSpecy
import com.example.pokedex.room.models.Type as RoomType

@OptIn(ExperimentalPagingApi::class)
class PokemonListRemoteMediator(
    private val apolloClient: ApolloClient,
    private val pokemonDao: PokemonDao,
    private val pagingDao: PagingDao
): RemoteMediator<Int, CompletePokemon>() {
    companion object {
        val cacheTimeout = 30.minutes
    }

    class PokemonIsNotCached(id: Int): Exception("Pokemon with id $id is not cached.")

    private suspend fun updatePage(pokemon: List<Pokemon>, offset: Int) {
        val roomPrimaryTypes =
            pokemon.asSequence().map(Pokemon::primaryType).map(RoomType::fromAppType)
        val roomSecondaryType = pokemon.asSequence().mapNotNull(Pokemon::secondaryType)
            .mapNotNull(RoomType::fromAppType)
        val roomTypes = (roomPrimaryTypes + roomSecondaryType).toList()
        val roomSpecies = pokemon.map(RoomSpecy::fromAppPokemon)
        val roomPokemon = pokemon.map(RoomPokemon::fromAppPokemon)
        val roomPokemonId = pokemon.map(Pokemon::id)

        pagingDao.updatePage(roomTypes, roomSpecies, roomPokemon, roomPokemonId, offset)
    }

    override suspend fun initialize(): InitializeAction {
        val lastUpdated = pagingDao.getPagingLastUpdated()
        if (lastUpdated == null) {
            Timber.d("Launch initial refresh")
            return InitializeAction.LAUNCH_INITIAL_REFRESH
        }

        return if (System.currentTimeMillis() - lastUpdated  <= cacheTimeout.inWholeMilliseconds) {
            Timber.d("Skip initial refresh")
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            Timber.d("Launch initial refresh")
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CompletePokemon>
    ): MediatorResult {
        return try {
            val offset = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> {
                    Timber.d("End of pagination reached.")
                    return MediatorResult.Success(endOfPaginationReached = true)
                }

                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    if (lastItem == null) {
                        /* In theory one would assume that list is never empty when
                        * appending, but for some reason this is the case after the first refresh.
                        * Therefore I set the offset to 0. */
                        0
                    } else {
                        // Continue from the last position
                        val lastPosition =
                            pagingDao.getOffsetByPokemonIdOrNull(pokemonId = lastItem.pokemonId)
                        Timber.d("LastPokemon: %d", lastItem.pokemonId)
                        Timber.d("LastPosition: %d", lastPosition)
                        if (lastPosition == null) {
                            Timber.d("LastPosition is null")
                            return MediatorResult.Error(PokemonIsNotCached(lastItem.pokemonId))
                        }
                        lastPosition + 1
                    }
                }
            }
            Timber.d("LoadOffset: %d", offset)

            val response = apolloClient
                .query(PokemonListPageQuery(state.config.pageSize, offset))
                .execute()
            val exception = response.exception
            if (exception != null) {
                Timber.e(exception)
                return MediatorResult.Error(exception)
            }
            val data = response.dataAssertNoErrors
            val pokemon = data.pokemon.map { pokemon ->
                Pokemon.fromApolloPokemon(pokemon.pokemonFragment)
            }

            if (loadType == LoadType.REFRESH) {
                pagingDao.clearPagingEntries()
            }
            updatePage(pokemon, offset)

            if (offset >= data.count.aggregate!!.count) {
                Timber.d("End of pagination reached.")
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            Timber.d("End of pagination not yet reached.")
            MediatorResult.Success(endOfPaginationReached = false)
        } catch (e: Exception) {
            Timber.e(e)
            assert(false)
            return MediatorResult.Error(e)
        }
    }
}