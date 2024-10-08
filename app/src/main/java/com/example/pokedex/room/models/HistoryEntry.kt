package com.example.pokedex.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pokedex.models.HistoryEntry
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import com.example.pokedex.models.HistoryEntry as AppHistoryEntry

@Entity(tableName = "history_entry")
data class HistoryEntry(
    @ColumnInfo(name = "timestamp_ms")
    val timestampMs: Long,
    @PrimaryKey
    @ColumnInfo(name = "query")
    val query: String
) {
    companion object {
        fun fromAppHistoryEntry(historyEntry: AppHistoryEntry) = HistoryEntry(
            query = historyEntry.query,
            timestampMs = historyEntry.dateTimeUTC.toInstant(ZoneOffset.UTC).toEpochMilli()
        )
    }
}