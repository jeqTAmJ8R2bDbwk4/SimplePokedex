package com.example.pokedex.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.pokedex.room.models.FavouriteEntry
import com.example.pokedex.room.models.HistoryEntry
import com.example.pokedex.room.models.PagingEntry
import com.example.pokedex.room.models.Pokemon
import com.example.pokedex.room.models.Specy
import com.example.pokedex.room.models.Type
import com.example.pokedex.room.views.CompletePokemon


@Database(
    entities = [
        HistoryEntry::class,
        Pokemon::class,
        Type::class,
        Specy::class,
        FavouriteEntry::class,
        PagingEntry::class,
    ],
    views = [CompletePokemon::class],
    version = 1
)
abstract class Database : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
    abstract fun favouriteDao(): FavouriteDao
    abstract fun historyDao(): HistoryDao
    abstract fun pagingDao(): PagingDao
}