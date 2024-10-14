package com.example.pokedex.repositories


import com.apollographql.apollo.ApolloClient
import com.example.pokedex.PokemonDetailsQuery
import com.example.pokedex.PokemonListByIdsQuery
import com.example.pokedex.PokemonNameSearchQuery
import com.example.pokedex.PokemonSearchQuery
import com.example.pokedex.models.Pokemon
import com.example.pokedex.models.PokemonDetails
import com.example.pokedex.models.PokemonMinimal
import com.example.pokedex.models.errors.ApolloError
import com.example.pokedex.models.errors.RepositoryError
import kotlinx.coroutines.CancellationException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class RemoteRepository @Inject constructor(
    private val apolloClient: ApolloClient,
) {
    companion object {
        private val NAME_IDS_OF_TOP_30_POKEMON_2020 = listOf(
            658, // Quajutsu
            448, // Lucario
            778, // Mimigma
            6,   // Glurak
            197, // Nachtara
            700, // Feelinara
            445, // Knakrack
            384, // Rayquaza
            282, // Guardevoir
            94,  // Gengar
            887, // Katapuldra
            248, // Despotar
            1,   // Bisasam
            849, // Riffex
            249, // Lugia
            722, // Bauz
            681, // Durengard
            609, // Skelabra
            25,  // Pikachu
            133, // Evoli
            405, // Luxtra
            724, // Silvarro
            571, // Zoroark
            745, // Wolwerock
            823, // Krarmor
            330, // Libelldra
            635, // Trikephalo
            254, // Gewaldro
            257, // Lohgock
            872, // Snomnom
        )
    }

    suspend fun getSuggestion(query: String): Result<List<String>, RepositoryError> {
        return try {
            require(!query.contains("%"))
            require(query == query.trim())

            val response = apolloClient
                .query(PokemonNameSearchQuery(query+"%"))
                .execute()
            val exception = response.exception
            if (exception != null) {
                Timber.e(exception)
                val apolloError = ApolloError.fromException(exception)
                return Result.Error(RepositoryError.ApolloError(apolloError))
            }
            val responseData = response.dataAssertNoErrors
            val result = (
                responseData.specyNames.asSequence().map { name -> name.name }
                + responseData.formNames.asSequence().map { name -> name.name }
            ).toList()
            Result.Success(result)
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            Timber.e(e)
            assert(false)
            Result.Error(RepositoryError.DataMappingException)
        }
    }

    suspend fun searchPokemon(query: String): Result<List<Pokemon>, RepositoryError> {
        return try {
            require(!query.contains("%"))
            require(query.trim() == query)
            val response = apolloClient
                .query(PokemonSearchQuery("%"+query+"%"))
                .execute()
            val exception = response.exception
            if (exception != null) {
                Timber.e(exception)
                val apolloError = ApolloError.fromException(exception)
                return Result.Error(RepositoryError.ApolloError(apolloError))
            }
            val responseData = response.dataAssertNoErrors
            val pokemonBySpecyName = responseData.pokemonBySpecyName.map { pokemon ->
                Pokemon.fromApolloPokemon(pokemon.pokemonFragment)
            }
            val pokemonByFormName = responseData.pokemonByFormName.map { pokemon ->
                Pokemon.fromApolloPokemon(pokemon.pokemonFragment)
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
            assert(false)
            Result.Error(RepositoryError.DataMappingException)
        }
    }

    suspend fun getPopularPokemon(): Result<List<PokemonMinimal>, RepositoryError> {
        return try {
            val response = apolloClient
                .query(PokemonListByIdsQuery(NAME_IDS_OF_TOP_30_POKEMON_2020))
                .execute()
            val exception = response.exception
            if (exception != null) {
                Timber.e(exception)
                val apolloError = ApolloError.fromException(exception)
                return Result.Error(RepositoryError.ApolloError(apolloError))
            }
            val responseData = response.dataAssertNoErrors
            val pokemonByIds = responseData.pokemon.map { pokemon -> pokemon.pokemonMinimalFragment }
            val pokemon = pokemonByIds
                .map(PokemonMinimal::fromApolloPokemon)
                .sortedWith(
                    compareBy { pokemon ->
                        NAME_IDS_OF_TOP_30_POKEMON_2020.indexOf(pokemon.id)
                    }
                )
            return Result.Success(pokemon)
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw  e
            }
            Timber.e(e)
            assert(false)
            Result.Error(RepositoryError.DataMappingException)
        }
    }

    suspend fun getPokemonDetails(id: Int): Result<PokemonDetails, RepositoryError> {
        return try {
            val response = apolloClient
                .query(PokemonDetailsQuery(id))
                .execute()
            val exception = response.exception
            if (exception != null) {
                Timber.e(exception)
                val apolloError = ApolloError.fromException(exception)
                return Result.Error(RepositoryError.ApolloError(apolloError))
            }
            val responseData = response.dataAssertNoErrors
            val pokemonDetails = PokemonDetails.fromApolloPokemonDetails(
                responseData.pokemon.first().pokemonDetailsFragment,
                responseData.type.map { typeRelation -> typeRelation.pokemonTypeRelationFragment },
                responseData.range_hp.pokemonStatsRangeFragment,
                responseData.range_attack.pokemonStatsRangeFragment,
                responseData.range_defense.pokemonStatsRangeFragment,
                responseData.range_sp_attack.pokemonStatsRangeFragment,
                responseData.range_sp_defense.pokemonStatsRangeFragment,
                responseData.range_speed.pokemonStatsRangeFragment
            )
            Result.Success(pokemonDetails)
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            Timber.e(e)
            assert(false)
            Result.Error(RepositoryError.DataMappingException)
        }
    }
}