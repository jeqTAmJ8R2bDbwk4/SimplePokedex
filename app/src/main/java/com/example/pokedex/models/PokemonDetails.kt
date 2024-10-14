package com.example.pokedex.models

import com.example.pokedex.utils.NonEmpty
import com.example.pokedex.utils.squeeze
import com.example.pokedex.utils.validateNonEmpty
import org.apache.commons.math3.fraction.Fraction
import com.example.pokedex.fragment.PokemonDetailsFragment as ApolloPokemon
import com.example.pokedex.fragment.PokemonStatsRangeFragment as ApolloStatRange
import com.example.pokedex.fragment.PokemonTypeRelationFragment as ApolloTypeRelation

data class PokemonDetails(
    // General
    val minSpecialAttack: Int,
    val maxSpecialAttack: Int,
    val minSpecialDefense: Int,
    val maxSpecialDefense: Int,
    val minSpeed: Int,
    val maxSpeed: Int,
    val minDefense: Int,
    val maxDefense: Int,
    val minAttack: Int,
    val maxAttack: Int,
    val minHP: Int,
    val maxHP: Int,

    // Pokemon
    val id: Int,
    val baseHP: Int,
    val baseAttack: Int,
    val baseSpecialDefense: Int,
    val baseSpeed: Int,
    val baseSpecialAttack: Int,
    val baseDefense: Int,
    val abilities: List<Ability>,
    val typeWeekness: Map<Fraction, List<Int>>,

    // Form
    val formName: String,
    val formLocalizedName: String,

    // Types
    val primaryType: Type,
    val secondaryType: Type?,

    val specyId: Int,
    @NonEmpty val specyName: String,
    val specyLocalizedName: String,
    val specyNationalPokedexNumber: Int,
    val specyDescriptions: List<Description>,
    val specyEvolutionChain: List<Pokemon>,
) {
    init { validateNonEmpty() }

    fun getName() = formLocalizedName.ifEmpty {
        specyLocalizedName.ifEmpty {
            formName.ifEmpty {
                specyName
            }
        }
    }

    companion object {
        @Throws(NullPointerException::class)
        fun fromApolloPokemonDetails(
            pokemon: ApolloPokemon,
            typeRelations: List<ApolloTypeRelation>,
            hpRange: ApolloStatRange,
            attackRange: ApolloStatRange,
            defenseRange: ApolloStatRange,
            specialAttackRange: ApolloStatRange,
            specialDefenseRange: ApolloStatRange,
            speedRange: ApolloStatRange
        ): PokemonDetails {
            val specy = pokemon.specy!!.pokemonSpecyDetailsFragment
            val types = pokemon.types.map { type ->
                type.pokemonTypesFragment.type!!.pokemonTypeFragment
            }
            val baseStats = pokemon.stats.map { stat -> stat.pokemonStatsFragment }
            val form = pokemon.forms.first().pokemonFormFragment
            val descriptions = specy.descriptions.map { description ->
                description.pokemonDescriptionFragment
            }

            val baseHP = baseStats.find { stat -> stat.stat!!.id == 1 }!!
            val baseAttack = baseStats.find { stat -> stat.stat!!.id == 2 }!!
            val baseDefense = baseStats.find { stat -> stat.stat!!.id == 3 }!!
            val baseSpecialAttack = baseStats.find { stat -> stat.stat!!.id == 4 }!!
            val baseSpecialDefense = baseStats.find { stat -> stat.stat!!.id == 5 }!!
            val baseSpeed = baseStats.find { stat -> stat.stat!!.id == 6 }!!

            val primaryType = types.first()
            val secondaryType = types.getOrNull(1)

            val typeWeekness = typeRelations.asSequence()
                .flatMap { type ->
                    type.efficacies.map { efficacy ->
                        Triple(
                            type.let { type -> type.id },
                            efficacy.target!!.let { type ->
                                type.id
                            },
                            Fraction(efficacy.damageFactor, 100)
                        )
                    }
                }
                .filter { (_, targetTypeId, _) ->
                    types.map { type -> type.id }.contains(targetTypeId)
                }
                .groupBy(Triple<Int, Int, Fraction>::first)
                .mapValues { (_, triples) ->
                    triples.map(Triple<Int, Int, Fraction>::third).reduce(Fraction::multiply)
                }
                .entries
                .asSequence()
                .filter { (_, damage) -> damage != Fraction.ONE }
                .groupBy(Map.Entry<Int, Fraction>::value, Map.Entry<Int, Fraction>::key)
                .filterValues(List<Int>::isNotEmpty)

            return PokemonDetails(
                minHP = hpRange.aggregate!!.min!!.baseStat!!,
                maxHP = hpRange.aggregate.max!!.baseStat!!,
                minAttack = attackRange.aggregate!!.min!!.baseStat!!,
                maxAttack = attackRange.aggregate.max!!.baseStat!!,
                minDefense = defenseRange.aggregate!!.min!!.baseStat!!,
                maxDefense = defenseRange.aggregate.max!!.baseStat!!,
                minSpecialAttack = specialAttackRange.aggregate!!.min!!.baseStat!!,
                maxSpecialAttack = specialAttackRange.aggregate.max!!.baseStat!!,
                minSpecialDefense = specialDefenseRange.aggregate!!.min!!.baseStat!!,
                maxSpecialDefense = specialDefenseRange.aggregate.max!!.baseStat!!,
                minSpeed = speedRange.aggregate!!.min!!.baseStat!!,
                maxSpeed = speedRange.aggregate.max!!.baseStat!!,

                id = pokemon.id,
                baseHP = baseHP.baseStat,
                baseAttack = baseAttack.baseStat,
                baseDefense = baseDefense.baseStat,
                baseSpecialAttack = baseSpecialAttack.baseStat,
                baseSpecialDefense = baseSpecialDefense.baseStat,
                baseSpeed = baseSpeed.baseStat,

                primaryType = Type(
                    id = primaryType.id,
                    name = primaryType.name,
                    localizedName = primaryType.names.firstOrNull()?.name.orEmpty()
                ),
                secondaryType = secondaryType?.let {
                    Type(
                        id = secondaryType.id,
                        name = secondaryType.name,
                        localizedName = secondaryType.names.firstOrNull()?.name.orEmpty()
                    )
                },
                formName = form.name,
                formLocalizedName = form.names.firstOrNull()?.name.orEmpty(),

                specyId = specy.pokemonSpecyFragment.id,
                specyName = specy.pokemonSpecyFragment.name,
                specyLocalizedName = specy
                    .pokemonSpecyFragment
                    .names
                    .firstOrNull()
                    ?.name
                    .orEmpty(),
                specyDescriptions = descriptions.map { description ->
                    Description(
                        gameVersionName = description.version!!.name,
                        localizedGameVersionName = description
                            .version
                            .names
                            .firstOrNull()
                            ?.name
                            .orEmpty(),
                        description = description.description.replace('\n', ' ')
                    )
                },

                abilities = pokemon.abilities.map { ability ->
                    Ability(
                        id = ability.pokemonAbilityFragment.ability!!.id,
                        name = ability.pokemonAbilityFragment.ability.name,
                        localizedName = ability
                            .pokemonAbilityFragment
                            .ability
                            .names
                            .firstOrNull()
                            ?.name
                            .orEmpty(),
                        isHidden = ability.pokemonAbilityFragment.isHidden,
                        descriptions = ability
                            .pokemonAbilityFragment
                            .ability
                            .descriptions
                            .map { description ->
                                AbilityDescription(
                                    text = description.description.replace('\n', ' '),
                                    versionGroupId = description.versionGroup!!.id,
                                    versionGroup = description
                                        .versionGroup
                                        .versions
                                        .map { version ->
                                            GameVersion(
                                                id = version.id,
                                                name = version.name,
                                                localizedName = version.names.firstOrNull()?.name.orEmpty()
                                            )
                                    }
                                )
                        }
                    )
                },

                specyEvolutionChain = specy.evolutionChain!!.species.map { specy ->
                    Pokemon.fromApolloPokemon(specy.pokemons.squeeze().pokemonFragment)
                },
                specyNationalPokedexNumber = specy
                    .pokemonSpecyFragment
                    .pokedex_numbers
                    .squeeze()
                    .pokedex_number,
                typeWeekness = typeWeekness
            )
        }
    }
}
