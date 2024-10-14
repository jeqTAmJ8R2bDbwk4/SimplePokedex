package com.example.pokedex.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "favourite_entry",
    foreignKeys = [
        ForeignKey(
            entity = Pokemon::class,
            parentColumns = ["id"],
            childColumns = ["pokemon_id"],
            // Don't want to accidentally delete Favourites of User:
            onDelete = ForeignKey.RESTRICT,
        ),
    ]
)
data class FavouriteEntry(
    @PrimaryKey
    @ColumnInfo(name = "pokemon_id", index = true)
    val pokemonId: Int,

    @ColumnInfo(name = "position", index = true)
    val position: Int,
)