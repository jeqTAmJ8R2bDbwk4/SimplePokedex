package com.example.pokedex.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

import com.example.pokedex.models.Pokemon as AppPokemon

@Entity(
    tableName = "pokemon",
    foreignKeys = [
        ForeignKey(
            entity = Specy::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("specy_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Type::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("primary_type_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Type::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("secondary_type_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Pokemon(
    @PrimaryKey()
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "specy_id", index = true)
    val specyId: Int,
    @ColumnInfo(name = "primary_type_id", index = true)
    val primaryTypeId: Int,
    @ColumnInfo(name = "secondary_type_id", index = true)
    val secondaryTypeId: Int?,
    @ColumnInfo(name = "sprite_url")
    val spriteUrl: String,
    @ColumnInfo(name = "official_sprite_url")
    val officialSpriteUrl: String,
    @ColumnInfo(name = "form_name")
    val formName: String,
    @ColumnInfo(name = "form_localized_name")
    val formLocalizedName: String
) {
    companion object {
        fun fromAppPokemon(pokemon: AppPokemon) = Pokemon(
            id = pokemon.id,
            specyId = pokemon.specyId,
            primaryTypeId = pokemon.primaryType.id,
            secondaryTypeId = pokemon.secondaryType?.id,
            spriteUrl = pokemon.spriteUrl,
            officialSpriteUrl = pokemon.officialSpriteUrl,
            formName = pokemon.formName,
            formLocalizedName = pokemon.formLocalizedName
        )
    }
}
