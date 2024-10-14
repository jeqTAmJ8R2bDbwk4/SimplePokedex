package com.example.pokedex.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.pokedex.room.models.HistoryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Upsert
    suspend fun upsertHistoryEntry(historyEntry: HistoryEntry)

    @Query("""
        SELECT
            *
        FROM
            history_entry HE
        ORDER BY
            HE.last_modified_epoch_ms DESC
        """)
    fun getHistoryFlow(): Flow<List<HistoryEntry>>
}