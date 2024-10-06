package com.example.pokedex.models

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import com.example.pokedex.room.models.HistoryEntry as RoomHistoryEntry

data class HistoryEntry(val query: String, val dateTimeUTC: LocalDateTime) {
    companion object {
        fun fromRoomHistoryEntry(historyEntry: RoomHistoryEntry) = HistoryEntry(
            query = historyEntry.query,
            dateTimeUTC = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(historyEntry.timestampMs),
                ZoneOffset.UTC
            )
        )
    }
}