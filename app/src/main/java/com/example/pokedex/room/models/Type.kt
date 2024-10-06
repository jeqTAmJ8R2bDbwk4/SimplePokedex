package com.example.pokedex.room.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pokedex.models.Type as AppType

@Entity(tableName = "type")
data class Type(
    @PrimaryKey()
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "localized_name")
    val localizedName: String,
) {
    companion object {
        fun fromAppType(type: AppType) = Type(
            id = type.id,
            name = type.name,
            localizedName = type.localizedName
        )
    }
}