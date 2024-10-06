package com.example.pokedex.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "list_entry",
    foreignKeys = [
        ForeignKey(
            entity = Pokemon::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("pokemon_id"),
            onDelete = ForeignKey.CASCADE,
        )
    ],
    primaryKeys = ["pokemon_id", "list_id"]
)
data class ListEntry(
    @ColumnInfo(name = "pokemon_id", index = true)
    val pokemonId: Int,

    @ColumnInfo(name = "position", index = true)
    val position: Int,

    @ColumnInfo(name = "list_id", index = true)
    val listId: Int
)
