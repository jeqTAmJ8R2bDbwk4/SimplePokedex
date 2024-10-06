package com.example.pokedex.repositories


import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.example.pokedex.PokemonDetailsQuery
import com.example.pokedex.PokemonNameSearchQuery
import com.example.pokedex.PokemonSearchQuery
import com.example.pokedex.models.Ability
import com.example.pokedex.models.AbilityDescription
import com.example.pokedex.models.Pokemon
import com.example.pokedex.models.Description
import com.example.pokedex.models.GameVersion
import com.example.pokedex.models.PokemonDetails
import com.example.pokedex.models.Type
import com.example.pokedex.models.errors.ApolloError
import com.example.pokedex.models.errors.RepositoryError
import com.example.pokedex.paging.PokemonListPagingSource
import com.example.pokedex.utils.squeeze
import kotlinx.coroutines.CancellationException
import org.apache.commons.math3.fraction.Fraction
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class RemoteRepository @Inject constructor(
    private val apolloClient: ApolloClient,
) {
    companion object {
        private val NAME_IDS_OF_TOP_30_POKEMON_2020 = intArrayOf(
            7233, // Quajutsu
            4923, // Lucario
            8553, // Mimigma
            61,   // Glurak
            2162, // Nachtara
            7695, // Feelinara
            4890, // Knakrack
            4219, // Rayquaza
            3097, // Guardevoir
            1029, // Gengar
            9752, // Katapuldra
            2723, // Despotar
            6,    // Bisasam
            9334, // Riffex
            2734, // Lugia
            7937, // Bauz
            7486, // Durengard
            6694, // Skelabra
            270,  // Pikachu
            1458, // Evoli
            4450, // Luxtra
            7959, // Silvarro
            6276, // Zoroark
            8190, // Wolwerock
            9048, // Krarmor
            3625, // Libelldra
            6980, // Trikephalo
            2789, // Gewaldro
            2822, // Lohgock
            9587  // Snomnom
        )
    }

    fun getAllPokemon(): Pager<Int, Pokemon> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = true
            ),
            pagingSourceFactory = { PokemonListPagingSource(apolloClient) }
        )
    }

    suspend fun getSuggestion(query: String): Result<List<String>, RepositoryError> {
        return try {
            require(!query.contains("%"))
            require(query == query.trim())

            val response = apolloClient
                .query(PokemonNameSearchQuery(Optional.present(query+"%")))
                .execute()
            val exception = response.exception
            if (exception != null) {
                Timber.e(exception)
                val apolloError = ApolloError.fromException(exception)
                return Result.Error(RepositoryError.ApolloError(apolloError))
            }
            val responseData = response.dataAssertNoErrors
            val result = (
                responseData.specy_names.asSequence().map { name -> name.name }
                + responseData.form_names.asSequence().map { name -> name.name }
            ).toList()
            Result.Success(result)
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            Timber.e(e)
            Result.Error(RepositoryError.DataMappingException)
        }
    }

    suspend fun searchPokemon(query: String): Result<List<Pokemon>, RepositoryError> {
        return try {
            require(!query.contains("%"))
            require(query.trim() == query)
            val response = apolloClient
                .query(PokemonSearchQuery(Optional.present("%"+query+"%")))
                .execute()
            val exception = response.exception
            if (exception != null) {
                Timber.e(exception)
                val apolloError = ApolloError.fromException(exception)
                return Result.Error(RepositoryError.ApolloError(apolloError))
            }
            val responseData = response.dataAssertNoErrors
            val pokemonBySpecyName = responseData.pokemon_by_specy_name.map { pokemon ->
                Pokemon.fromApolloPokemon(pokemon.pokemon_fragment)
            }
            val pokemonByFormName = responseData.pokemon_by_form_name.map { pokemon ->
                Pokemon.fromApolloPokemon(pokemon.pokemon_fragment)
            }
            val pokemon = (pokemonByFormName.asSequence() + pokemonBySpecyName.asSequence())
                .distinctBy(Pokemon::id)
                .sortedWith(compareBy(Pokemon::getName, Pokemon::specyId, Pokemon::id))
                .toList()
            Result.Success(pokemon)
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            Timber.e(e)
            Result.Error(RepositoryError.DataMappingException)
        }
    }

    suspend fun getPokemonDetails(id: Int): Result<PokemonDetails, RepositoryError> {
        return try {
            val response = apolloClient
                .query(PokemonDetailsQuery(Optional.present(id)))
                .execute()
            val exception = response.exception
            if (exception != null) {
                Timber.e(exception)
                val apolloError = ApolloError.fromException(exception)
                return Result.Error(RepositoryError.ApolloError(apolloError))
            }
            val responseData = response.dataAssertNoErrors
            val pokemonDetails = PokemonDetails.fromApolloPokemonDetails(
                responseData.pokemon.first().pokemon_details_fragment,
                responseData.type.map { typeRelation -> typeRelation.pokemon_type_relation_fragment },
                responseData.range_hp.pokemon_stats_range_fragment,
                responseData.range_attack.pokemon_stats_range_fragment,
                responseData.range_defense.pokemon_stats_range_fragment,
                responseData.range_sp_attack.pokemon_stats_range_fragment,
                responseData.range_sp_defense.pokemon_stats_range_fragment,
                responseData.range_speed.pokemon_stats_range_fragment
            )
            Result.Success(pokemonDetails)
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            Timber.e(e)
            Result.Error(RepositoryError.DataMappingException)
        }
    }
}