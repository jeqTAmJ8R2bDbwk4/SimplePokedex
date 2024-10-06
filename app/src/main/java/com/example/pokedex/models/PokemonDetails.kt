package com.example.pokedex.models

import com.example.pokedex.fragment.Pokemon_details_fragment as ApolloPokemon
import com.example.pokedex.fragment.Pokemon_type_relation_fragment as ApolloTypeRelation
import com.example.pokedex.fragment.Pokemon_stats_range_fragment as ApolloStatRange
import com.example.pokedex.utils.NonEmpty
import com.example.pokedex.utils.squeeze
import com.example.pokedex.utils.validateNonEmpty
import org.apache.commons.math3.fraction.Fraction

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

    fun getName() = formLocalizedName.ifEmpty { specyLocalizedName.ifEmpty { formName.ifEmpty { specyName } } }

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
            val specy = pokemon.specy!!.pokemon_specy_details_fragment
            val types = pokemon.types.map { type -> type.pokemon_types_fragment.type!!.pokemon_type_fragment }
            val baseStats = pokemon.stats.map { stat -> stat.pokemon_stats_fragment }
            val form = pokemon.forms.first().pokemon_form_fragment
            val descriptions = specy.descriptions.map { description -> description.pokemon_description_fragment }

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
                            Fraction(efficacy.damage_factor, 100)
                        )
                    }
                }
                .filter { (_, targetTypeId, _) -> types.map { type -> type.id }.contains(targetTypeId) }
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
                minHP = hpRange.aggregate!!.min!!.base_stat!!,
                maxHP = hpRange.aggregate.max!!.base_stat!!,
                minAttack = attackRange.aggregate!!.min!!.base_stat!!,
                maxAttack = attackRange.aggregate.max!!.base_stat!!,
                minDefense = defenseRange.aggregate!!.min!!.base_stat!!,
                maxDefense = defenseRange.aggregate.max!!.base_stat!!,
                minSpecialAttack = specialAttackRange.aggregate!!.min!!.base_stat!!,
                maxSpecialAttack = specialAttackRange.aggregate.max!!.base_stat!!,
                minSpecialDefense = specialDefenseRange.aggregate!!.min!!.base_stat!!,
                maxSpecialDefense = specialDefenseRange.aggregate.max!!.base_stat!!,
                minSpeed = speedRange.aggregate!!.min!!.base_stat!!,
                maxSpeed = speedRange.aggregate.max!!.base_stat!!,

                id = pokemon.id,
                baseHP = baseHP.base_stat,
                baseAttack = baseAttack.base_stat,
                baseDefense = baseDefense.base_stat,
                baseSpecialAttack = baseSpecialAttack.base_stat,
                baseSpecialDefense = baseSpecialDefense.base_stat,
                baseSpeed = baseSpeed.base_stat,

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

                specyId = specy.pokemon_specy_fragment.id,
                specyName = specy.pokemon_specy_fragment.name,
                specyLocalizedName = specy.pokemon_specy_fragment.names.firstOrNull()?.name.orEmpty(),
                specyDescriptions = descriptions.map { description ->
                    Description(
                        gameVersionName = description.version!!.name,
                        localizedGameVersionName = description.version.names.firstOrNull()?.name.orEmpty(),
                        description = description.description.replace('\n', ' ')
                    )
                },

                abilities = pokemon.abilities.map { ability ->
                    Ability(
                        id = ability.pokemon_ability.ability!!.id,
                        name = ability.pokemon_ability.ability.name,
                        localizedName = ability.pokemon_ability.ability.names.firstOrNull()?.name.orEmpty(),
                        isHidden = ability.pokemon_ability.is_hidden,
                        descriptions = ability.pokemon_ability.ability.descriptions.map { description ->
                            AbilityDescription(
                                text = description.description.replace('\n', ' '),
                                versionGroupId = description.version_group!!.id,
                                versionGroup = description.version_group.versions.map { version ->
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

                specyEvolutionChain = specy.evolution_chain!!.species.map { specy -> Pokemon.fromApolloPokemon(specy.pokemons.squeeze().pokemon_fragment) },
                specyNationalPokedexNumber = specy.pokemon_specy_fragment.pokedex_numbers.squeeze().pokedex_number,
                typeWeekness = typeWeekness
            )
        }
    }
}
