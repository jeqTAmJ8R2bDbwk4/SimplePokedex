package com.example.pokedex.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "paging_entry",
    foreignKeys = [
        ForeignKey(
            entity = Pokemon::class,
            parentColumns = ["id"],
            childColumns = ["pokemon_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["offset"], unique = true)
    ]
)
data class PagingEntry(
    @PrimaryKey()
    @ColumnInfo(name = "pokemon_id", index = true)
    val pokemonId: Int,

    @ColumnInfo(name = "offset")
    val offset: Int,

    @ColumnInfo(name = "last_modified_epoch_ms", index = true)
    val lastModifiedEpochMs: Long
)