package com.example.pokedex.room

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object RoomModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): Database {
        return Room.databaseBuilder(
            context,
            Database::class.java,
            "database"
        ).build()
    }

    @Provides
    fun providePokemonDao(database: Database) = database.pokemonDao()

    @Provides
    fun provideFavouriteDao(database: Database) = database.favouriteDao()

    @Provides
    fun provideHistoryDao(database: Database) = database.historyDao()

    @Provides
    fun providePagingDao(database: Database) = database.pagingDao()
}