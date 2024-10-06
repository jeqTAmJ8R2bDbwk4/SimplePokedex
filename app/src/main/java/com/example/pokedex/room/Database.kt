package com.example.pokedex.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.pokedex.room.models.ListEntry
import com.example.pokedex.room.models.Pokemon
import com.example.pokedex.room.models.HistoryEntry
import com.example.pokedex.room.models.Type
import com.example.pokedex.room.models.Specy
import com.example.pokedex.room.views.CompletePokemon


@Database(
    entities = [
        HistoryEntry::class,
        ListEntry::class,
        Pokemon::class,
        Type::class,
        Specy::class
    ],
    views = [CompletePokemon::class],
    version = 1
)
abstract class Database : RoomDatabase() {
    abstract fun dao(): Dao
}