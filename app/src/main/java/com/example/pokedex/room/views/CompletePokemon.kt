package com.example.pokedex.room.views

import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@DatabaseView(
    value = """SELECT
        P.id AS pokemon_id,
        P.primary_type_id AS pokemon_primary_type_id,
        P.secondary_type_id AS pokemon_secondary_type_id,
        P.sprite_url AS pokemon_sprite_url,
        P.official_sprite_url AS pokemon_official_sprite_url,
        P.specy_id AS pokemon_specy_id,
        P.form_name AS pokemon_form_name,
        P.form_localized_name AS pokemon_form_localized_name,
        S.id AS specy_id,
        S.name AS specy_name,
        S.localized_name AS specy_localized_name,
        S.national_pokedex_number AS specy_national_pokedex_number,
        S.evolved_from_specy_id AS specyEvolvedFromSpecyId,
        PT.id AS primary_type_id,
        PT.name AS primary_type_name,
        PT.localized_name AS primary_type_localized_name,
        ST.id AS secondary_type_id,
        ST.name AS secondary_type_name,
        ST.localized_name AS secondary_type_localized_name
    FROM
        pokemon P
        JOIN type PT ON P.primary_type_id = PT.id
        LEFT JOIN type ST ON P.secondary_type_id = ST.id
        JOIN specy S ON P.specy_id = S.id""",
    viewName = "complete_pokemon"
)
data class CompletePokemon(
    // Pokemon
    @ColumnInfo(name = "pokemon_id")
    val pokemonId: Int,

    @ColumnInfo(name = "pokemon_specy_id")
    val pokemonSpecyId: Int,

    @ColumnInfo(name = "pokemon_secondary_type_id")
    val pokemonSecondaryTypeId: Int?,

    @ColumnInfo(name = "pokemon_primary_type_id")
    val pokemonPrimaryTypeId: Int,

    @ColumnInfo(name = "pokemon_sprite_url")
    val pokemonSpriteUrl: String,

    @ColumnInfo(name = "pokemon_official_sprite_url")
    val pokemonOfficialSpriteUrl: String,

    @ColumnInfo(name = "pokemon_form_name")
    val formName: String,

    @ColumnInfo(name = "pokemon_form_localized_name")
    val formLocalizedName: String,

    // Specy
    @ColumnInfo(name = "specy_id")
    val specyId: Int,

    @ColumnInfo(name = "specy_localized_name")
    val specyLocalizedName: String,

    @ColumnInfo(name = "specy_name")
    val specyName: String,

    @ColumnInfo(name = "specy_national_pokedex_number")
    val specyNationalPokedexNumber: Int,

    @ColumnInfo(name = "specy_evolved_from_specy_id")
    val specyEvolvedFromSpecyId: Int?,

    // Primary type
    @ColumnInfo(name = "primary_type_id")
    val primaryTypeId: Int,

    @ColumnInfo(name = "primary_type_localized_name")
    val primaryTypeLocalizedName: String,

    @ColumnInfo(name = "primary_type_name")
    val primaryTypeName: String,


    // Secondary type
    @ColumnInfo(name = "secondary_type_id")
    val secondaryTypeId: Int?,

    @ColumnInfo(name = "secondary_type_name")
    val secondaryTypeName: String?,

    @ColumnInfo(name = "secondary_type_localized_name")
    val secondaryTypeLocalizedName: String?,
)