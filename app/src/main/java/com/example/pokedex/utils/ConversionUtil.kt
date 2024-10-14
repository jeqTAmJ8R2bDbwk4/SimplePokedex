package com.example.pokedex.utils

import java.time.DateTimeException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val ROOM_DATETIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME

@Throws(DateTimeParseException::class, DateTimeException::class)
fun dateTimeFromRoomDatetime(dateTime: String): OffsetDateTime {
    return ROOM_DATETIME_FORMATTER.parse(dateTime, OffsetDateTime::from)
}

@Throws(DateTimeException::class)
fun dateTimeToRoomDatetime(dateTime: OffsetDateTime): String {
    return dateTime.format(ROOM_DATETIME_FORMATTER)
}