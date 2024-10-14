package com.example.pokedex.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pokedex.models.HistoryEntry as AppHistoryEntry

@Entity(tableName = "history_entry")
data class HistoryEntry(
    @ColumnInfo(name = "last_modified_epoch_ms")
    val lastModifiedEpochMs: Long,
    @PrimaryKey
    @ColumnInfo(name = "query")
    val query: String
) {
    companion object {
        fun fromAppHistoryEntry(historyEntry: AppHistoryEntry) = HistoryEntry(
            query = historyEntry.query,
            lastModifiedEpochMs = System.currentTimeMillis()
        )
    }
}