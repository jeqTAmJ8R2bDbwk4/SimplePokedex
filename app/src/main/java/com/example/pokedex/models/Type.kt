package com.example.pokedex.models

import android.os.Parcelable
import com.example.pokedex.utils.NonEmpty
import com.example.pokedex.utils.validateNonEmpty
import kotlinx.parcelize.Parcelize

@Parcelize data class Type(
    val id: Int,
    @NonEmpty val name: String,
    val localizedName: String
) : Parcelable {
    init { validateNonEmpty() }

    @JvmName(name = "getNameKotlin")
    fun getName() = localizedName.ifEmpty(::name)
}